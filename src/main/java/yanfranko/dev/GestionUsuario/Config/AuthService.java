package yanfranko.dev.GestionUsuario.Config; // Cambiado a .Config para mejor orden

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import yanfranko.dev.GestionUsuario.DTOs.Auth.AuthRequest;
import yanfranko.dev.GestionUsuario.DTOs.Auth.AuthResponse;
import yanfranko.dev.GestionUsuario.DTOs.Usuario.UsuarioRequestDTO;
import yanfranko.dev.GestionUsuario.DTOs.Usuario.UsuarioResponseDTO;
import yanfranko.dev.GestionUsuario.Models.Usuario;
import yanfranko.dev.GestionUsuario.Repository.UsuarioRepository;
import yanfranko.dev.GestionUsuario.Service.UsuarioService;
import java.util.Optional;

@Service
public class AuthService {

    private final UsuarioRepository usuarioRepository;
    private final UsuarioService usuarioService;
    private final JwtService jwtService;
    private final PasswordEncoder passwordEncoder;

    public AuthService(UsuarioRepository usuarioRepository,
            UsuarioService usuarioService,
            JwtService jwtService,
            PasswordEncoder passwordEncoder) {
        this.usuarioRepository = usuarioRepository;
        this.usuarioService = usuarioService;
        this.jwtService = jwtService;
        this.passwordEncoder = passwordEncoder;
    }

    /**
     * Registro: Delega el guardado al UsuarioService (que encripta)
     * y luego genera el Token.
     */
    public AuthResponse registrar(UsuarioRequestDTO dto) {
        // 1. Guardamos al usuario (UsuarioService se encarga de los datos y BCrypt)
        UsuarioResponseDTO usuarioGuardado = usuarioService.registrarUsuario(dto);

        // 2. Devolvemos la respuesta SIN TOKEN
        return new AuthResponse(
                null, // access token
                null, // refresh token
                "Registro exitoso. Por favor, inicia sesión para continuar.");
    }

    /**
     * Login: Valida credenciales y entrega el Token.
     */
    public AuthResponse login(AuthRequest request) {
        // 1. Buscar usuario por email y validar credenciales en un solo flujo
        Optional<Usuario> maybeUser = usuarioRepository.findByEmail(request.email());

        if (maybeUser.isEmpty() || !passwordEncoder.matches(request.password(), maybeUser.get().getPassword())) {
            throw new InvalidCredentialsException("Credenciales inválidas");
        }

        Usuario usuario = maybeUser.get();

        // 2. Generar los Tokens
        String accessToken = jwtService.generarAccessToken(usuario);
        String refreshToken = jwtService.generarRefreshToken(usuario);

        // 3. Devolver respuesta
        return new AuthResponse(
                accessToken,
                refreshToken,
                "¡Login exitoso! Bienvenido de nuevo.");
    }

    /**
     * Refresh Token: Valida el refresh token y genera un nuevo access token con
     * roles frescos.
     */
    public AuthResponse refreshToken(String refreshToken) {
        String email = jwtService.extractUsername(refreshToken);
        if (email == null || !jwtService.esTokenValido(refreshToken, email)) {
            throw new InvalidCredentialsException("Refresh token inválido");
        }

        Usuario usuario = usuarioRepository.findByEmail(email)
                .orElseThrow(() -> new InvalidCredentialsException("Usuario no encontrado"));

        String newAccessToken = jwtService.generarAccessToken(usuario);

        return new AuthResponse(
                newAccessToken,
                refreshToken, // reutilizar el mismo refresh token o generar nuevo
                "Token refrescado exitosamente.");
    }
}