package yanfranko.dev.GestionUsuario.Models;


import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor

@Entity
@Table(name = "usuarios")
public class Usuario {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long id;

    @Column(nullable = false, length = 50, unique = false)
    @NotBlank(message = "El nombre es OBLIGATORIO")
    @Size(min = 3, max = 50, message = "El nombre debe tener entre 3 y 50 caracteres")
    String nombre;

    @Column(nullable = false, length = 50, unique = false)
    @NotBlank(message = "El apellido es OBLIGATORIO")
    @Size(min = 3, max = 50, message = "El apellido debe tener entre 3 y 50 caracteres")
    String apellido;

    @Column(nullable = false, length = 50, unique = true)
    @Email(message = "El email debe ser válido")
    @NotBlank(message = "El email es OBLIGATORIO")
    String email;

    @Column(nullable = false, length = 2, unique = false)
    @Min(value = 0, message = "La edad debe ser un número positivo")
    @Max(value = 120, message = "La edad debe ser menor o igual a 120")
    int edad;

    @Column(nullable = false, length = 15, unique = false)
    @NotBlank(message = "El teléfono es OBLIGATORIO")
    String telefono;

    @Column(nullable = false, length = 8, unique = true)
    @NotBlank(message = "El DNI es OBLIGATORIO")
    @Size(min = 8, max = 8, message = "El DNI debe tener entre 8 caracteres")
    @Pattern(regexp = "\\d{8}", message = "El DNI debe contener solo números y tener exactamente 8 dígitos")
    String dni;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "role_id", nullable = false)
    private Role role;
    
    String password;
}
