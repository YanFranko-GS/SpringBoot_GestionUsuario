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
            null, // <--- Enviamos el token como null o vacío
            "Registro exitoso. Por favor, inicia sesión para continuar."
    );
}

    /**
     * Login: Valida credenciales y entrega el Token.
     */
    public AuthResponse login(AuthRequest request) {
        // 1. Buscar usuario por email
        Usuario usuario = usuarioRepository.findByEmail(request.email())
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

        // 2. Verificar contraseña usando matches (compara texto plano vs hash)
        if (!passwordEncoder.matches(request.password(), usuario.getPassword())) {
            throw new RuntimeException("Contraseña incorrecta");
        }

        // 3. Generar el Token
        String token = jwtService.generarToken(usuario);

        // 4. Devolver respuesta
        return new AuthResponse(
                token,
                "¡Login exitoso! Bienvenido de nuevo.");
    }
}