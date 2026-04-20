package yanfranko.dev.GestionUsuario.DTOs.Usuario;

import yanfranko.dev.GestionUsuario.Models.Usuario;

public record UsuarioResponseDTO(
    Long id,
    String nombre,
    String apellido,
    String email,
    int edad,
    String telefono,
    String dni
) {
    // Método estático opcional para convertir de Entidad a DTO rápidamente
    public static UsuarioResponseDTO fromEntity(Usuario usuario) {
        return new UsuarioResponseDTO(
            usuario.getId(),
            usuario.getNombre(),
            usuario.getApellido(),
            usuario.getEmail(),
            usuario.getEdad(),
            usuario.getTelefono(),
            usuario.getDni()
        );
    }
}