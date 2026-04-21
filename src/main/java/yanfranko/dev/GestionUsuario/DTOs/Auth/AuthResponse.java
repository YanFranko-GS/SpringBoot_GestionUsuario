package yanfranko.dev.GestionUsuario.DTOs.Auth;

//esta es la ultima vercion para el swuaguer 3.0.2 lo pongo aqui 
// por que ya no modificare este Auth

// DTO que enviamos al cliente cuando el login es correcto.

//esto me botara si el login es correcto, el token y un mensaje de bienvenida
public record AuthResponse(
        String token,
        String refreshToken,
        String mensaje) {
}