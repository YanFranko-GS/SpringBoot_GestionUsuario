# Gestión de Usuarios - Spring Boot API

Una aplicación REST API construida con Spring Boot para la gestión completa de usuarios, incluyendo autenticación JWT con refresh tokens, control de acceso basado en roles (RBAC) y operaciones CRUD seguras.

## 📋 Descripción

Esta API permite gestionar usuarios con diferentes roles (USER, MODERATOR, ADMIN), proporcionando autenticación segura mediante JWT tokens de corta duración y refresh tokens para mantener sesiones activas. Incluye validaciones, encriptación de contraseñas y protección contra vulnerabilidades comunes.

## ✨ Características

- 🔐 **Autenticación JWT** con access tokens (15 min) y refresh tokens (7 días)
- 👥 **Sistema de Roles** (USER, MODERATOR, ADMIN) con RBAC
- 🔒 **Seguridad Avanzada**: BCrypt para contraseñas, validaciones de entrada, CORS
- 📊 **Operaciones CRUD** completas para usuarios
- 🛡️ **Protección CSRF** deshabilitada (adecuado para APIs stateless)
- 📖 **Documentación Swagger** integrada
- 🗄️ **Base de Datos MySQL** con JPA/Hibernate
- ✅ **Validaciones** con Jakarta Validation
- 🚀 **Configuración Flexible** vía variables de entorno

## 🛠️ Tecnologías Utilizadas

- **Backend**: Java 21, Spring Boot 3.2+
- **Seguridad**: Spring Security 6, JWT (JJWT)
- **Base de Datos**: MySQL 8.0+
- **ORM**: JPA/Hibernate
- **Validación**: Jakarta Bean Validation
- **Documentación**: OpenAPI/Swagger 3
- **Build Tool**: Maven
- **Encriptación**: BCrypt

## 📦 Instalación

### Prerrequisitos

- Java 21+
- Maven 3.6+
- MySQL 8.0+
- Git

### Clonación del Repositorio

```bash
git clone https://github.com/tu-usuario/gestion-usuarios.git
cd gestion-usuarios
```

### Configuración de Base de Datos

1. Instalar MySQL y crear la base de datos:
```sql
CREATE DATABASE gestion_usuarios_db;
```

2. Configurar credenciales en variables de entorno:
```bash
export DB_USERNAME=tu_usuario_mysql
export DB_PASSWORD=tu_password_mysql
export JWT_SECRET=tu_secreto_jwt_seguro_de_al_menos_32_caracteres
export SHOW_SQL=false  # true para desarrollo
```

### Compilación y Ejecución

```bash
# Compilar
./mvnw clean compile

# Ejecutar
./mvnw spring-boot:run
```

La aplicación estará disponible en `http://localhost:8080`

## ⚙️ Configuración

### Variables de Entorno

| Variable | Descripción | Valor por Defecto |
|----------|-------------|-------------------|
| `DB_USERNAME` | Usuario MySQL | `root` |
| `DB_PASSWORD` | Contraseña MySQL | `root` |
| `JWT_SECRET` | Secreto para JWT | Requerido |
| `SHOW_SQL` | Mostrar queries SQL | `false` |

### application.properties

```properties
# Base de datos
spring.datasource.url=jdbc:mysql://localhost:3306/gestion_usuarios_db?createDatabaseIfNotExist=true&serverTimezone=UTC
spring.datasource.username=${DB_USERNAME:root}
spring.datasource.password=${DB_PASSWORD:root}

# JPA
spring.jpa.hibernate.ddl-auto=update
spring.jpa.show-sql=${SHOW_SQL:false}

# Servidor
server.port=8080
server.servlet.context-path=/

# JWT
jwt.secret=${JWT_SECRET:}

# Swagger
springdoc.swagger-ui.path=/swagger-ui.html
```

## 🚀 Uso

### Documentación API

Accede a la documentación interactiva en:
- Swagger UI: `http://localhost:8080/swagger-ui.html`
- OpenAPI JSON: `http://localhost:8080/v3/api-docs`

### Endpoints Principales

#### Autenticación

##### Registro
```http
POST /api/v1/auth/register
Content-Type: application/json

{
  "nombre": "Juan",
  "apellido": "Pérez",
  "email": "juan@example.com",
  "edad": 30,
  "telefono": "123456789",
  "dni": "12345678",
  "password": "password123"
}
```

##### Login
```http
POST /api/v1/auth/login
Content-Type: application/json

{
  "email": "juan@example.com",
  "password": "password123"
}
```

**Respuesta:**
```json
{
  "token": "eyJhbGciOiJIUzI1NiJ9...",
  "refreshToken": "eyJhbGciOiJIUzI1NiJ9...",
  "mensaje": "¡Login exitoso! Bienvenido de nuevo."
}
```

##### Refresh Token
```http
POST /api/v1/auth/refresh
Content-Type: application/json

{
  "refreshToken": "eyJhbGciOiJIUzI1NiJ9..."
}
```

#### Usuarios (Requiere Autenticación)

**Headers requeridos:**
```
Authorization: Bearer <access_token>
```

##### Listar Usuarios
```http
GET /api/v1/usuarios
Authorization: Bearer <token>
```

##### Obtener Usuario por ID
```http
GET /api/v1/usuarios/{id}
Authorization: Bearer <token>
```

##### Crear Usuario (ADMIN/MODERATOR)
```http
POST /api/v1/usuarios
Authorization: Bearer <token>
Content-Type: application/json

{
  "nombre": "María",
  "apellido": "García",
  "email": "maria@example.com",
  "edad": 25,
  "telefono": "987654321",
  "dni": "87654321",
  "roleId": 1,
  "password": "securePass123"
}
```

##### Actualizar Usuario
```http
PUT /api/v1/usuarios/{id}
Authorization: Bearer <token>
Content-Type: application/json

{
  "nombre": "María",
  "apellido": "García",
  "email": "maria@example.com",
  "edad": 26,
  "telefono": "987654321",
  "dni": "87654321",
  "roleId": 1,
  "password": "newSecurePass123"
}
```

##### Actualización Parcial
```http
PATCH /api/v1/usuarios/{id}
Authorization: Bearer <token>
Content-Type: application/json

{
  "edad": 27,
  "telefono": "999888777"
}
```

##### Eliminar Usuario (ADMIN)
```http
DELETE /api/v1/usuarios/{id}
Authorization: Bearer <token>
```

## 🔐 Seguridad

### Autenticación JWT

- **Access Token**: Expira en 15 minutos, incluye roles del usuario
- **Refresh Token**: Expira en 7 días, usado para obtener nuevos access tokens
- **Roles**: USER, MODERATOR, ADMIN con permisos jerárquicos

### Control de Acceso (RBAC)

| Endpoint | USER | MODERATOR | ADMIN |
|----------|------|-----------|-------|
| GET /usuarios | ✅ | ✅ | ✅ |
| POST /usuarios | ❌ | ✅ | ✅ |
| PUT /usuarios | ❌ | ✅ | ✅ |
| PATCH /usuarios | ❌ | ✅ | ✅ |
| DELETE /usuarios | ❌ | ❌ | ✅ |

### Validaciones de Seguridad

- ✅ Contraseñas encriptadas con BCrypt
- ✅ Validación de entrada con anotaciones
- ✅ Protección contra SQL Injection (JPA)
- ✅ Tokens JWT firmados con HMAC-SHA256
- ✅ Configuración segura de headers HTTP
- ✅ Variables de entorno para secrets

## 🏗️ Arquitectura

```
src/main/java/yanfranko/dev/GestionUsuario/
├── Config/                 # Configuración de seguridad y JWT
│   ├── AuthService.java
│   ├── JwtService.java
│   ├── SecurityConfig.java
│   └── ...
├── Controller/             # Controladores REST
│   ├── AuthController.java
│   └── UsuarioController.java
├── DTOs/                   # Data Transfer Objects
│   ├── Auth/
│   └── Usuario/
├── Models/                 # Entidades JPA
│   ├── Usuario.java
│   └── Role.java
├── Repository/             # Interfaces de repositorio
├── Service/                # Lógica de negocio
└── GestionUsuarioApplication.java
```

### Capas de la Aplicación

1. **Controller**: Maneja requests HTTP, validaciones básicas
2. **Service**: Lógica de negocio, validaciones complejas
3. **Repository**: Acceso a datos con JPA
4. **Config**: Configuración de seguridad y beans

## 🧪 Pruebas

```bash
# Ejecutar todas las pruebas
./mvnw test

# Ejecutar con cobertura
./mvnw test jacoco:report
```

## 📊 Base de Datos

### Esquema

```sql
-- Usuarios
CREATE TABLE usuarios (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    nombre VARCHAR(50) NOT NULL,
    apellido VARCHAR(50) NOT NULL,
    email VARCHAR(50) UNIQUE NOT NULL,
    edad INT NOT NULL,
    telefono VARCHAR(15),
    dni VARCHAR(8) UNIQUE NOT NULL,
    password VARCHAR(255) NOT NULL,
    role_id BIGINT NOT NULL,
    FOREIGN KEY (role_id) REFERENCES roles(id)
);

-- Roles
CREATE TABLE roles (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    e_role ENUM('ROLE_USER', 'ROLE_MODERATOR', 'ROLE_ADMIN') UNIQUE
);
```

## 🚀 Despliegue

### Docker (Opcional)

```dockerfile
FROM openjdk:21-jdk-slim
COPY target/*.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java","-jar","/app.jar"]
```

```bash
# Construir imagen
docker build -t gestion-usuarios .

# Ejecutar
docker run -p 8080:8080 \
  -e DB_USERNAME=user \
  -e DB_PASSWORD=pass \
  -e JWT_SECRET=secret \
  gestion-usuarios
```

### Producción

- Configurar variables de entorno seguras
- Usar HTTPS
- Configurar logging apropiado
- Monitoreo y alertas

## 🤝 Contribución

1. Fork el proyecto
2. Crea una rama para tu feature (`git checkout -b feature/nueva-funcionalidad`)
3. Commit tus cambios (`git commit -am 'Agrega nueva funcionalidad'`)
4. Push a la rama (`git push origin feature/nueva-funcionalidad`)
5. Abre un Pull Request

## 📝 Licencia

Este proyecto está bajo la Licencia MIT. Ver el archivo `LICENSE` para más detalles.

## 📞 Soporte

Para soporte técnico o preguntas:
- Email: soporte@gestionusuarios.com
- Issues: [GitHub Issues](https://github.com/tu-usuario/gestion-usuarios/issues)

---

**Desarrollado con ❤️ usando Spring Boot**
