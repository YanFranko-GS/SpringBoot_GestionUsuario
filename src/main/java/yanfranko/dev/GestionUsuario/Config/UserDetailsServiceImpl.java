package yanfranko.dev.GestionUsuario.Config;

import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import yanfranko.dev.GestionUsuario.Models.Usuario;
import yanfranko.dev.GestionUsuario.Repository.UsuarioRepository;

@Service
public class UserDetailsServiceImpl implements UserDetailsService {

    private final UsuarioRepository usuarioRepository;

    public UserDetailsServiceImpl(UsuarioRepository usuarioRepository) {
        this.usuarioRepository = usuarioRepository;
    }

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        // Buscamos al usuario en tu repositorio
        Usuario usuario = usuarioRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("Usuario no encontrado con email: " + email));

        // Convertimos tu entidad 'Usuario' al 'User' que entiende Spring Security
        return User.builder()
                .username(usuario.getEmail())
                .password(usuario.getPassword()) // Debe estar encriptada en la BD
                .authorities(usuario.getRole().getERole().name()) // Aquí pasamos el ROL
                .build();
    }
}