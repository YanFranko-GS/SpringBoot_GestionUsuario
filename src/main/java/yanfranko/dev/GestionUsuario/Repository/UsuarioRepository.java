package yanfranko.dev.GestionUsuario.Repository;


import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import yanfranko.dev.GestionUsuario.Models.Usuario;
import yanfranko.dev.GestionUsuario.Models.Enum.ERole;

@Repository
public interface UsuarioRepository extends JpaRepository<Usuario, Long> {
    
    //su List cuando estas buscando Alos usuarios por su rol o etc
    //se una optional cuando vas a buscar algo unico por ejemplo el correo, DNI, ect es unico para cada usuario


    //falta implementar en el service
    //busca un usuario por su email
    Optional<Usuario> findByEmail(String email);


    //ya esta implementado en el service y controller
    //busca un usuario por su DNI
    Optional<Usuario> findByDni(String dni);


    //ya esta implementado en el service y controller
    //verifica si existe el email en la base de datos
    Boolean existsByEmail(String email);

    // Lista todos los usuario que tiene un rol especifico, por ejemplo: ROLE_ADMIN, ROLE_USER, etc
    List<Usuario> findByRole_eRole(ERole role);

    
}
