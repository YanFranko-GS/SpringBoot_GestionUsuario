package yanfranko.dev.GestionUsuario.DTOs.Rol;


import yanfranko.dev.GestionUsuario.Models.Enum.ERole;
import yanfranko.dev.GestionUsuario.Models.Role;

/**
 * Este DTO es la "tarjeta de presentación" del Rol.
 * Se usa para mostrar la información al usuario sin exponer 
 * la entidad JPA completa.
 */
public record RolResponseDTO(
    Long id,
    ERole role
) {
    /**
     * Método estático para transformar una Entidad en un DTO.
     * Es como pasar de un objeto complejo de base de datos a un formato ligero.
     */
    public static RolResponseDTO fromEntity(Role role) {
        return new RolResponseDTO(
            role.getId(),
            role.getERole()
        );
    }
}