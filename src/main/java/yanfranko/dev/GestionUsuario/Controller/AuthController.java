package yanfranko.dev.GestionUsuario.Controller;

import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import yanfranko.dev.GestionUsuario.Config.AuthService;
import yanfranko.dev.GestionUsuario.DTOs.Auth.AuthRequest;
import yanfranko.dev.GestionUsuario.DTOs.Auth.AuthResponse;
import yanfranko.dev.GestionUsuario.DTOs.Usuario.UsuarioRequestDTO;


@RestController
@RequestMapping("/api/v1/auth")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    /**
     * Registro de nuevos usuarios. 
     * No devuelve Token, obliga al usuario a loguearse después.
     */
    @PostMapping("/register")
    public ResponseEntity<AuthResponse> registrar(@RequestBody @Valid UsuarioRequestDTO dto) {
        return new ResponseEntity<>(authService.registrar(dto), HttpStatus.CREATED);
    }

    /**
     * Inicio de sesión. 
     * Verifica credenciales y entrega el JWT (Token).
     */
    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@RequestBody @Valid AuthRequest request) {
        return ResponseEntity.ok(authService.login(request));
    }
}