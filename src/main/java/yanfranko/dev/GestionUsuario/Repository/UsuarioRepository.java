package yanfranko.dev.GestionUsuario.Repository;


import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import yanfranko.dev.GestionUsuario.Models.Usuario;

@Repository
public interface UsuarioRepository extends JpaRepository<Usuario, Long> {
    
    //falta implementar en el service
    //busca un usuario por su email
    Optional<Usuario> findByEmail(String email);


    //ya esta implementado en el service y controller
    //busca un usuario por su DNI
    Optional<Usuario> findByDni(String dni);


    //ya esta implementado en el service y controller
    //verifica si existe el email en la base de datos
    Boolean existsByEmail(String email);


    
}
