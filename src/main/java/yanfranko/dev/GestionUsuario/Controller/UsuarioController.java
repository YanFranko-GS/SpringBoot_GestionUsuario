package yanfranko.dev.GestionUsuario.Controller;

import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import yanfranko.dev.GestionUsuario.DTOs.Usuario.UsuarioRequestDTO;
import yanfranko.dev.GestionUsuario.DTOs.Usuario.UsuarioResponseDTO;
import yanfranko.dev.GestionUsuario.Models.Enum.ERole;
import yanfranko.dev.GestionUsuario.Service.UsuarioService;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/usuarios") // Estándar de versionamiento de APIs
public class UsuarioController {

    private final UsuarioService usuarioService;

    public UsuarioController(UsuarioService usuarioService) {
        this.usuarioService = usuarioService;
    }

    // CREATE
    @PostMapping
    public ResponseEntity<UsuarioResponseDTO> crear(@RequestBody @Valid UsuarioRequestDTO dto) {
        UsuarioResponseDTO nuevoUsuario = usuarioService.registrarUsuario(dto);
        return new ResponseEntity<>(nuevoUsuario, HttpStatus.CREATED);
    }

    // READ (Todos)
    @GetMapping
    public ResponseEntity<List<UsuarioResponseDTO>> listar() {
        return ResponseEntity.ok(usuarioService.listarTodo());
    }

    // READ (Uno solo)
    @GetMapping("/{id}")
    public ResponseEntity<UsuarioResponseDTO> obtenerUno(@PathVariable Long id) {
        return ResponseEntity.ok(usuarioService.buscarPorId(id));
    }

    // READ (Buscar por DNI)
    @GetMapping("/dni/{dni}")
    public ResponseEntity<UsuarioResponseDTO> buscarPorDni(@PathVariable String dni) {
        UsuarioResponseDTO usuario = usuarioService.buscarPorDni(dni);
        return ResponseEntity.ok(usuario);
    }

    // Lista a todos los que tienen un rol específico, por ejemplo: ROLE_ADMIN, ROLE_USER, etc
    @GetMapping("/rol/{nombreRol}")
    public ResponseEntity<List<UsuarioResponseDTO>> listarPorRol(@PathVariable("nombreRol") ERole role) {
    // Pasamos la variable 'rol' que recibimos de la URL al service
    List<UsuarioResponseDTO> usuarios = usuarioService.buscarPorRol(role); 
    return ResponseEntity.ok(usuarios);
}

    // UPDATE
    @PutMapping("/{id}")
    public ResponseEntity<UsuarioResponseDTO> actualizar(@PathVariable Long id,
            @RequestBody @Valid UsuarioRequestDTO dto) {
        return ResponseEntity.ok(usuarioService.actualizarUsuario(id, dto));
    }

    @PatchMapping("/{id}")
    public ResponseEntity<UsuarioResponseDTO> actualizarParcial(
            @PathVariable Long id,
            @RequestBody Map<String, Object> campos) {
//        UsuarioResponseDTO actualizado = usuarioService.actualizarParcial(id, campos);
//        return ResponseEntity.ok(actualizado);
        return ResponseEntity.ok(usuarioService.actualizarParcial(id, campos));
    }

    // DELETE
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(@PathVariable Long id) {
        usuarioService.eliminarUsuario(id);
        return ResponseEntity.noContent().build(); // Devuelve un 204 (Sin contenido)
    }
}