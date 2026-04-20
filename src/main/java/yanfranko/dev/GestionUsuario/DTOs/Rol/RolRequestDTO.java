package yanfranko.dev.GestionUsuario.DTOs.Rol;

import jakarta.validation.constraints.NotNull;
import yanfranko.dev.GestionUsuario.Models.Enum.ERole;

public record RolRequestDTO(
    @NotNull(message = "El nombre del rol es obligatorio")
    ERole role // Recibimos el Enum (ROLE_ADMIN, etc.)
) {}