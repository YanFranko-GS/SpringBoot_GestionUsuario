# Auditoría de Seguridad y Código — GestionUsuario

Fecha: 2026-04-19

Resumen ejecutivo
- Alcance: revisión de configuración de seguridad JWT, controladores de autenticación/usuarios, servicios, modelo y dependencias.
- Objetivo: encontrar vulnerabilidades, bugs y recomendar correcciones prácticas.

Archivos revisados (selección)
- [src/main/java/yanfranko/dev/GestionUsuario/Config/JwtService.java](src/main/java/yanfranko/dev/GestionUsuario/Config/JwtService.java)
- [src/main/java/yanfranko/dev/GestionUsuario/Config/JwtAuthenticationFilter.java](src/main/java/yanfranko/dev/GestionUsuario/Config/JwtAuthenticationFilter.java)
- [src/main/java/yanfranko/dev/GestionUsuario/Config/AuthService.java](src/main/java/yanfranko/dev/GestionUsuario/Config/AuthService.java)
- [src/main/java/yanfranko/dev/GestionUsuario/Service/UsuarioService.java](src/main/java/yanfranko/dev/GestionUsuario/Service/UsuarioService.java)
- [src/main/resources/application.properties](src/main/resources/application.properties)
- [pom.xml](pom.xml)

Hallazgos críticos

- **Secretos en el código / credenciales en properties**: El `JWT secret` y credenciales DB estaban definidos en código/`application.properties`. Riesgo: exposición en VCS y entornos.
  - Impacto: compromiso de tokens y DB si el repo se filtra.
  - Recomendación: usar variables de entorno o vault (ej. `JWT_SECRET`, `DB_URL`, `DB_USER`, `DB_PASS`).

- **Parsing/validación JWT incorrecto**: uso anterior de una API no estándar para extraer claims podía dejar la firma sin verificar o arrojar excepciones no controladas.
  - Impacto: tokens inválidos podrían producir 500s o bypass si no se valida correctamente.
  - Recomendación: usar `Jwts.parserBuilder().setSigningKey(...).build().parseClaimsJws(token).getBody()` y manejar `JwtException`.

- **Mensajes de login que permiten enumeración de usuarios**: respuestas distintas para "usuario no encontrado" vs "contraseña incorrecta".
  - Impacto: facilita reconocimiento de cuentas válidas.
  - Recomendación: devolver error genérico para credenciales inválidas (401) y registrar internamente.

Hallazgos importantes

- **Actualización parcial insegura (PATCH)**: casting directo desde `Map<String,Object>` sin validar tipos puede causar `ClassCastException` o introducir datos inválidos. Además, cambiar roles no estaba restringido.
  - Recomendación: validar tipos/valores, usar DTOs de patch o helpers de conversión y restringir cambio de rol a administradores.

- **Hibernate `ddl-auto=update` y `createDatabaseIfNotExist=true`** en `application.properties` — peligro en producción.
  - Recomendación: usar `validate` y gestion de migraciones con Flyway/Liquibase en prod.

- **Falta de protección contra fuerza bruta** en `/api/v1/auth/login`.
  - Recomendación: añadir rate-limiting (Bucket4j, Redis, API gateway), bloqueo temporal tras N intentos.

- **Mezcla de versiones de Jackson** detectada en `dependency:tree` (Jackson 3.x y 2.x presentes debido a `spring-boot` vs `jjwt-jackson`).
  - Impacto: errores en tiempo de ejecución (NoSuchMethodError / Incompatibilidades).
  - Recomendación: alinear versiones de Jackson (forzar a una versión única vía `dependencyManagement` o excluir transitive de `jjwt-jackson`).

Cambios aplicados (en el repo)

- `JwtService.java`: externalizado `jwt.secret` (lee `jwt.secret` desde `application.properties`/`JWT_SECRET`), usa `parserBuilder()` para validar JWS y maneja `JwtException`.
- `AuthService.java`: unificación de respuesta de login; ahora lanza `InvalidCredentialsException` para credenciales inválidas.
- `ApiExceptionHandler.java`: `@RestControllerAdvice` para mapear `InvalidCredentialsException` a 401 y errores de configuración a 500.
- `JwtAuthenticationFilter.java`: manejo de excepciones al extraer claims para evitar 500s y proceder sin autenticar si el token es inválido.
- `UsuarioService.java`: refactor de `actualizarParcial` — validaciones de tipo, helpers de conversión, y restricción de `roleId` solo para `ROLE_ADMIN`.
- `application.properties`: añadido `jwt.secret=${JWT_SECRET:}` para incentivar uso de env var.

Pruebas y validación recomendadas (rápido)

1. Definir variable de entorno JWT y arrancar la app localmente:

```bash
export JWT_SECRET="cambiar_por_secreto_seguro_32+"
./mvnw -DskipTests spring-boot:run
```

2. Ejecutar el árbol de dependencias (ya ejecutado aquí):

```bash
./mvnw -DskipTests dependency:tree
```

3. Ejecutar un escaneo de vulnerabilidades OWASP (opcional):

```bash
./mvnw org.owasp:dependency-check-maven:check -Dformat=HTML -DoutputDirectory=dependency-check-report
```

Recomendaciones de mitigación prioritarias (acciones concretas)

- Mover secretos/credenciales a variables de entorno o secret manager. Verificar `JwtService` y `application.properties`.
- Corregir la mezcla de Jackson:
  - Opción A (rápida): excluir `jackson-databind` de `jjwt-jackson` y forzar la versión 3.x en `dependencyManagement`.
  - Opción B (más segura): buscar una versión de `jjwt-jackson` compatible con Jackson 3.x o usar la implementación de JJWT que no dependa de Jackson (o adaptar un módulo que use Jackson 3).
  - Ejemplo de exclusión (pom.xml):

```xml
<dependency>
  <groupId>io.jsonwebtoken</groupId>
  <artifactId>jjwt-jackson</artifactId>
  <version>0.12.6</version>
  <scope>runtime</scope>
  <exclusions>
    <exclusion>
      <groupId>com.fasterxml.jackson.core</groupId>
      <artifactId>jackson-databind</artifactId>
    </exclusion>
  </exclusions>
</dependency>
```

- Implementar rate-limiting en `/api/v1/auth/login` (Bucket4j o middleware). Añadir tests que simulen varios intentos fallidos.
- Reemplazar `RuntimeException` genérico por excepciones específicas y usar `ApiExceptionHandler` para respuestas consistentes.
- En producción: cambiar `spring.jpa.hibernate.ddl-auto` a `validate` y habilitar migraciones con Flyway.

Siguientes pasos que puedo ejecutar por ti

- A: Aplicar la exclusión/forzado de Jackson en `pom.xml` y probar arranque.
- B: Ejecutar OWASP Dependency-Check y adjuntar reporte HTML.
- C: Implementar rate-limiting básico en el endpoint `AuthController`.
- D: Generar reporte final descargable (este archivo) y cerrar la auditoría.

Changelog (archivos editados)
- [src/main/java/yanfranko/dev/GestionUsuario/Config/JwtService.java](src/main/java/yanfranko/dev/GestionUsuario/Config/JwtService.java)
- [src/main/java/yanfranko/dev/GestionUsuario/Config/AuthService.java](src/main/java/yanfranko/dev/GestionUsuario/Config/AuthService.java)
- [src/main/java/yanfranko/dev/GestionUsuario/Config/InvalidCredentialsException.java](src/main/java/yanfranko/dev/GestionUsuario/Config/InvalidCredentialsException.java)
- [src/main/java/yanfranko/dev/GestionUsuario/Config/ApiExceptionHandler.java](src/main/java/yanfranko/dev/GestionUsuario/Config/ApiExceptionHandler.java)
- [src/main/java/yanfranko/dev/GestionUsuario/Config/JwtAuthenticationFilter.java](src/main/java/yanfranko/dev/GestionUsuario/Config/JwtAuthenticationFilter.java)
- [src/main/java/yanfranko/dev/GestionUsuario/Service/UsuarioService.java](src/main/java/yanfranko/dev/GestionUsuario/Service/UsuarioService.java)
- [src/main/resources/application.properties](src/main/resources/application.properties)

Si quieres que aplique cualquiera de las acciones A/B/C, indícame cuál y la implemento y pruebo.

---
Generado automáticamente por la auditoría solicitada.
