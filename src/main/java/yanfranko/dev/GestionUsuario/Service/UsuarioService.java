package yanfranko.dev.GestionUsuario.Service;

import java.util.List;
import java.util.Map;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
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
    private final PasswordEncoder passwordEncoder;

    public UsuarioService(UsuarioRepository usuarioRepository, RolRepository rolRepository,
            PasswordEncoder passwordEncoder) {
        this.usuarioRepository = usuarioRepository;
        this.rolRepository = rolRepository;
        this.passwordEncoder = passwordEncoder;
    }

    // CREATE
    @Transactional
    public UsuarioResponseDTO registrarUsuario(UsuarioRequestDTO dto) {
        if (usuarioRepository.existsByEmail(dto.email())) {
            throw new IllegalArgumentException("El email ya está registrado");
        }

        // ASIGNACIÓN AUTOMÁTICA: Buscamos el rol 'USER' en la BD
        // Línea 38 corregida:
        Role role = rolRepository.findByERole(ERole.ROLE_USER) // <-- Nota la E mayúscula
                .orElseThrow(() -> new RuntimeException("Error: El rol USER no existe en la base de datos."));

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

        if (!usuarioExistente.getEmail().equals(dto.email()) && usuarioRepository.existsByEmail(dto.email())) {
            throw new RuntimeException("El nuevo email ya está en uso por otro usuario");
        }

        // Aquí sí usamos el roleId del DTO porque es una actualización (posiblemente de
        // un Admin)
        if (dto.roleId() != null) {
            Role role = rolRepository.findById(dto.roleId())
                    .orElseThrow(() -> new RuntimeException("Rol no encontrado con ID: " + dto.roleId()));
            usuarioExistente.setRole(role);
        }

        mapDtoToEntity(dto, usuarioExistente);
        Usuario usuarioActualizado = usuarioRepository.save(usuarioExistente);
        return UsuarioResponseDTO.fromEntity(usuarioActualizado);
    }

    // PATCH (actualización parcial)
    // esto es opcional pero es una buena práctica para no tener que enviar todos
    // los campos en una actualización, solo los que queremos cambiar
    @Transactional
    public UsuarioResponseDTO actualizarParcial(Long id, Map<String, Object> campos) {
        Usuario usuario = usuarioRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado con ID: " + id));
        for (Map.Entry<String, Object> entry : campos.entrySet()) {
            String key = entry.getKey();
            Object value = entry.getValue();

            switch (key) {
                case "nombre" -> {
                    String nombre = toStringSafe(value);
                    if (nombre == null || nombre.length() < 3 || nombre.length() > 50) {
                        throw new IllegalArgumentException("Nombre inválido");
                    }
                    usuario.setNombre(nombre);
                }
                case "apellido" -> {
                    String apellido = toStringSafe(value);
                    if (apellido == null || apellido.length() < 3 || apellido.length() > 50) {
                        throw new IllegalArgumentException("Apellido inválido");
                    }
                    usuario.setApellido(apellido);
                }
                case "email" -> {
                    String nuevoEmail = toStringSafe(value);
                    if (nuevoEmail == null || !nuevoEmail.matches("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$"))
                        throw new IllegalArgumentException("Email inválido");
                    if (!usuario.getEmail().equals(nuevoEmail) && usuarioRepository.existsByEmail(nuevoEmail)) {
                        throw new RuntimeException("El email ya está en uso");
                    }
                    usuario.setEmail(nuevoEmail);
                }
                case "edad" -> {
                    Integer edad = toInteger(value);
                    if (edad == null || edad < 0 || edad > 120)
                        throw new IllegalArgumentException("Edad inválida");
                    usuario.setEdad(edad);
                }
                case "telefono" -> {
                    String telefono = toStringSafe(value);
                    if (telefono == null || telefono.length() > 15)
                        throw new IllegalArgumentException("Teléfono inválido");
                    usuario.setTelefono(telefono);
                }
                case "dni" -> {
                    String dni = toStringSafe(value);
                    if (dni == null || !dni.matches("\\d{8}"))
                        throw new IllegalArgumentException("DNI inválido");
                    usuario.setDni(dni);
                }
                case "roleId" -> {
                    // Solo ADMIN puede cambiar roles
                    Authentication auth = SecurityContextHolder.getContext().getAuthentication();
                    boolean isAdmin = auth != null && auth.getAuthorities().stream()
                            .anyMatch(a -> "ROLE_ADMIN".equals(a.getAuthority()));
                    if (!isAdmin)
                        throw new AccessDeniedException("Solo administradores pueden cambiar roles");

                    Long roleId = toLong(value);
                    if (roleId == null)
                        throw new IllegalArgumentException("roleId inválido");
                    Role role = rolRepository.findById(roleId)
                            .orElseThrow(() -> new RuntimeException("Rol no encontrado"));
                    usuario.setRole(role);
                }
                case "password" -> {
                    String pass = toStringSafe(value);
                    if (pass == null || pass.length() < 8)
                        throw new IllegalArgumentException("La contraseña debe tener al menos 8 caracteres");
                    usuario.setPassword(passwordEncoder.encode(pass));
                }
                default -> {
                    // Ignorar campos desconocidos para evitar errores
                }
            }
        }

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

    // Método privado para mapear DTO a entidad, centralizando la lógica de mapeo
    private void mapDtoToEntity(UsuarioRequestDTO dto, Usuario usuario) {
        usuario.setNombre(dto.nombre());
        usuario.setApellido(dto.apellido());
        usuario.setEmail(dto.email());
        usuario.setEdad(dto.edad());
        usuario.setTelefono(dto.telefono());
        usuario.setDni(dto.dni());
        usuario.setPassword(passwordEncoder.encode(dto.password()));
    }

    // Helpers para conversiones seguras desde Map<String,Object>
    private Integer toInteger(Object value) {
        if (value == null)
            return null;
        if (value instanceof Number)
            return ((Number) value).intValue();
        try {
            return Integer.parseInt(value.toString());
        } catch (Exception e) {
            return null;
        }
    }

    private Long toLong(Object value) {
        if (value == null)
            return null;
        if (value instanceof Number)
            return ((Number) value).longValue();
        try {
            return Long.parseLong(value.toString());
        } catch (Exception e) {
            return null;
        }
    }

    private String toStringSafe(Object value) {
        if (value == null)
            return null;
        return value.toString().trim();
    }
}
