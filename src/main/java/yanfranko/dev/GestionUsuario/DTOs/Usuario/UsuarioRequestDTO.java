package yanfranko.dev.GestionUsuario.DTOs.Usuario;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;

import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record UsuarioRequestDTO(
    @NotBlank(message = "El nombre no puede estar vacío")
    @Size(min = 3, max = 50)
    String nombre,

    @NotBlank(message = "El apellido no puede estar vacío")
    @Size(min = 3, max = 50)
    String apellido,

    @Email(message = "Email inválido")
    @NotBlank(message = "El email es obligatorio")
    String email,

    @Min(0) @Max(120)
    int edad,

    @NotBlank(message = "El teléfono es obligatorio")
    String telefono,

    @Pattern(regexp = "\\d{8}", message = "El DNI debe tener 8 dígitos")
    String dni,

    //@NotNull(message = "El ID del rol es obligatorio")
    @Schema(hidden = true)
    Long roleId,

    @NotBlank(message = "La contraseña es obligatoria")
    @Size(min = 8, message = "La contraseña debe tener al menos 8 caracteres")
    String password
)
{}
