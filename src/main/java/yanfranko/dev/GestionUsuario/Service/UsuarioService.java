package yanfranko.dev.GestionUsuario.Service;

import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import yanfranko.dev.GestionUsuario.DTOs.Usuario.UsuarioRequestDTO;
import yanfranko.dev.GestionUsuario.DTOs.Usuario.UsuarioResponseDTO;
import yanfranko.dev.GestionUsuario.Models.Role;
import yanfranko.dev.GestionUsuario.Models.Usuario;
import yanfranko.dev.GestionUsuario.Models.Enum.ERole;
import yanfranko.dev.GestionUsuario.Repository.RolRepository;
import yanfranko.dev.GestionUsuario.Repository.UsuarioRepository;

@Service
public class UsuarioService {
    private final UsuarioRepository usuarioRepository;
    private final RolRepository rolRepository;

    public UsuarioService(UsuarioRepository usuarioRepository, RolRepository rolRepository) {
        this.usuarioRepository = usuarioRepository;
        this.rolRepository = rolRepository;
    }

    // CREATE
    @Transactional
    public UsuarioResponseDTO registrarUsuario(UsuarioRequestDTO dto) {
        if (usuarioRepository.existsByEmail(dto.email())) {
            throw new RuntimeException("El email ya está registrado");
        }
        Role role = rolRepository.findById(Long.valueOf(dto.roleId()))
                .orElseThrow(() -> new RuntimeException("Rol no encontrado con ID: " + dto.roleId() + "no existe"));

        Usuario usuario = new Usuario();
        mapDtoToEntity(dto, usuario);
        usuario.setRole(role);

        Usuario usuarioGuardado = usuarioRepository.save(usuario);
        return UsuarioResponseDTO.fromEntity(usuarioGuardado);
    }

    // LISTAR TODO
    @Transactional(readOnly = true)
    public List<UsuarioResponseDTO> listarTodo() {
        return usuarioRepository.findAll()
                .stream()
                .map(UsuarioResponseDTO::fromEntity)
                .toList();
    }

    // BUSCAR POR ID
    @Transactional(readOnly = true)
    public UsuarioResponseDTO buscarPorId(Long id) {
        Usuario usuario = usuarioRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado con ID: " + id));
        return UsuarioResponseDTO.fromEntity(usuario);
    }

    // BUSCAR POR DNI
    @Transactional(readOnly = true)
    public UsuarioResponseDTO buscarPorDni(String dni) {
        Usuario usuario = usuarioRepository.findByDni(dni)
                .orElseThrow(() -> new RuntimeException("No se encontró un usuario con el DNI: " + dni));
        return UsuarioResponseDTO.fromEntity(usuario);
    }

    // BUSCAR POR ROL
    @Transactional(readOnly = true)
    public List<UsuarioResponseDTO> buscarPorRol(ERole role) { // Recibe el Enum directamente
    return usuarioRepository.findByRole_eRole(role)
            .stream()
            .map(UsuarioResponseDTO::fromEntity)
            .toList();
}

    // UPDATE
    @Transactional
    public UsuarioResponseDTO actualizarUsuario(Long id, UsuarioRequestDTO dto) {
        Usuario usuarioExistente = usuarioRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("No se puede actualizar, usuario no encontrado"));
        // Validar si quiere cambiar el email a uno que ya existe (y no es el suyo)
        if (!usuarioExistente.getEmail().equals(dto.email()) && usuarioRepository.existsByEmail(dto.email())) {
            throw new RuntimeException("El nuevo email ya está en uso por otro usuario");
        }

        Role role = rolRepository.findById(dto.roleId())
                .orElseThrow(() -> new RuntimeException("Rol no encontrado con ID: " + dto.roleId()));

        mapDtoToEntity(dto, usuarioExistente);
        usuarioExistente.setRole(role);
        Usuario usuarioActualizado = usuarioRepository.save(usuarioExistente);
        return UsuarioResponseDTO.fromEntity(usuarioActualizado);
    }

    // PATCH (actualización parcial)
    @Transactional
    public UsuarioResponseDTO actualizarParcial(Long id, Map<String, Object> campos) {
        // 1. Buscar al usuario o lanzar error
        Usuario usuario = usuarioRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado con ID: " + id));

        // 2. Iterar sobre los campos recibidos y aplicar solo los que existan
        campos.forEach((key, value) -> {
            switch (key) {
                case "nombre" -> usuario.setNombre((String) value);
                case "apellido" -> usuario.setApellido((String) value);
                case "email" -> {
                    String nuevoEmail = (String) value;
                    if (!usuario.getEmail().equals(nuevoEmail) && usuarioRepository.existsByEmail(nuevoEmail)) {
                        throw new RuntimeException("El email ya está en uso");
                    }
                    usuario.setEmail(nuevoEmail);
                }
                case "edad" -> usuario.setEdad((Integer) value);
                case "telefono" -> usuario.setTelefono((String) value);
                case "dni" -> usuario.setDni((String) value);
                case "roleId" -> {
                    // Map recibe los números como Integer
                    Role role = rolRepository.findById(Long.valueOf(value.toString()))
                            .orElseThrow(() -> new RuntimeException("Rol no encontrado"));
                    usuario.setRole(role);
                }
                case "password" -> usuario.setPassword((String) value); // Aquí iría el BCrypt
            }
        });

        // 3. Guardar cambios y devolver la respuesta
        return UsuarioResponseDTO.fromEntity(usuarioRepository.save(usuario));
    }

    // DELETE
    @Transactional
    public void eliminarUsuario(Long id) {
        if (!usuarioRepository.existsById(id)) {
            throw new RuntimeException("No se pudo eliminar, usuario no encontrado");
        }
        usuarioRepository.deleteById(id);
    }

    // si en un futuro queremos agregar más campos o lógica, este método privado nos
    // ayuda a no repetir código
    // Además, centraliza la lógica de mapeo entre DTO y Entidad, lo que facilita el
    // mantenimiento
    // Por ejemplo, si luego queremos agregar validaciones adicionales o transformar
    // datos (como encriptar la contraseña), solo tendríamos que modificar este
    // método
    // También mejora la legibilidad del código en los métodos públicos, ya que se
    // abstrae el detalle de cómo se mapean los campos
    // Nota: En un proyecto real, probablemente usaríamos una librería como
    // MapStruct para manejar este tipo de conversiones de manera más elegante y
    // menos propensa a errores humanos.
    // ¡Ojo! Este método asume que el DTO tiene todos los campos necesarios. Si el
    // DTO es parcial (como en un PATCH), habría que adaptar la lógica para no
    // sobrescribir campos con null o valores no deseados.
    // En el caso de la contraseña, es importante recordar que en un proyecto real
    // no deberíamos almacenar ni manejar contraseñas en texto plano. Aquí se
    // muestra así por simplicidad, pero en una aplicación real deberíamos usar
    // BCrypt o alguna otra técnica de hashing para proteger las contraseñas.
    // Además, si el DTO es para creación (POST) y otro para actualización
    // (PUT/PATCH), podríamos tener métodos separados para mapear cada uno, ya que
    // podrían tener diferentes campos requeridos o lógica de validación.
    // En resumen, este método privado es una buena práctica para evitar la
    // duplicación de código y centralizar la lógica de mapeo entre DTOs y
    // entidades, lo que facilita el mantenimiento y la evolución del código a
    // medida que el proyecto crece.
    // Por ejemplo, si luego queremos agregar un nuevo campo "dirección" al usuario,
    // solo tendríamos que agregarlo en el DTO, en la entidad y luego actualizar
    // este método para mapear ese nuevo campo, sin tener que tocar la lógica de los
    // métodos públicos.
    // Además, si en el futuro decidimos cambiar la forma en que manejamos las
    // contraseñas (por ejemplo, agregando un campo de "salt" o usando una
    // estrategia de hashing diferente), solo tendríamos que modificar este método
    // para aplicar esa lógica de manera consistente en toda la aplicación.
    // En definitiva, este método privado es una pieza clave para mantener un código
    // limpio, modular y fácil de mantener a largo plazo.
    // Nota final: En un proyecto real, también podríamos considerar el uso de un
    // patrón de diseño como el "Mapper" o "Converter" para manejar estas
    // conversiones de manera más estructurada, especialmente si tenemos muchas
    // entidades y DTOs en la aplicación. Esto ayudaría a mantener el código aún más
    // organizado y separado por responsabilidades.
    // En resumen, la idea es evitar la duplicación de código y centralizar la
    // lógica de mapeo para facilitar el mantenimiento y la evolución del proyecto a
    // medida que crece en complejidad.
    // En este caso, el método "mapDtoToEntity" es un ejemplo de cómo podemos
    // centralizar la lógica de mapeo entre un DTO de solicitud y una entidad de
    // usuario, lo que nos permite mantener un código más limpio y fácil de mantener
    // a largo plazo.

    // ojo si quieres implementar otro atributo ejemplo: "direccion" tienes que añadir 
    // en el model y en el UsuarioRequestDTO y luego agregarlo en este método para que se mapee correctament
    // Método privado auxiliar para evitar repetir los "setters"
    private void mapDtoToEntity(UsuarioRequestDTO dto, Usuario usuario) {
        usuario.setNombre(dto.nombre());
        usuario.setApellido(dto.apellido());
        usuario.setEmail(dto.email());
        usuario.setEdad(dto.edad());
        usuario.setTelefono(dto.telefono());
        usuario.setDni(dto.dni());
        usuario.setPassword(dto.password()); // Recuerda: ¡aquí debería ir BCrypt!
    }
}
