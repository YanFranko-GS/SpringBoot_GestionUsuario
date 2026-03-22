package yanfranko.dev.GestionUsuario.Config;

import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import yanfranko.dev.GestionUsuario.Models.Enum.ERole;
import yanfranko.dev.GestionUsuario.Models.Role;
import yanfranko.dev.GestionUsuario.Repository.RolRepository;

import java.util.Arrays;



@Configuration
public class DataInitializer {

    @Bean
    CommandLineRunner initRoles(RolRepository rolRepository) {
        return args -> {
            // Recorremos todos los valores de nuestro Enum ERole
            Arrays.stream(ERole.values()).forEach(rolEnum -> {
                
                // CAMBIO AQUÍ: Debe ser existsByERole para que coincida con tu Repository
                if (!rolRepository.existsByERole(rolEnum)) {
                    Role nuevoRol = new Role();
                    nuevoRol.setERole(rolEnum);
                    rolRepository.save(nuevoRol);
                    System.out.println("Rol creado automáticamente: " + rolEnum);
                }
            });
        };
    }
}