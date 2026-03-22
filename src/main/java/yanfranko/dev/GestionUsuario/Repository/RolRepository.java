package yanfranko.dev.GestionUsuario.Repository;

import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import yanfranko.dev.GestionUsuario.Models.Role;
import yanfranko.dev.GestionUsuario.Models.Enum.ERole;

@Repository
public interface RolRepository extends JpaRepository<Role, Long> {

    // Usamos JPQL manual para asegurar que busque 'eRole' correctamente
    @Query("SELECT r FROM Role r WHERE r.eRole = :eRole")
    Optional<Role> findByERole(@Param("eRole") ERole eRole);

    @Query("SELECT COUNT(r) > 0 FROM Role r WHERE r.eRole = :eRole")
    boolean existsByERole(@Param("eRole") ERole eRole);
}