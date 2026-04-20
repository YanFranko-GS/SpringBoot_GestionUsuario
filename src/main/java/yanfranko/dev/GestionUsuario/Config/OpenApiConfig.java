package yanfranko.dev.GestionUsuario.Config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.security.SecurityRequirement; 
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
public OpenAPI customOpenAPI() {
    final String securitySchemeName = "bearerAuth";
    
    // 1. Creamos el requerimiento de seguridad
    SecurityRequirement securityRequirement = new SecurityRequirement()
            .addList(securitySchemeName);

    return new OpenAPI()
            .info(new Info().title("Yan Franko API").version("1.0"))
            // 2. Usamos el método correcto para añadirlo globalmente
            .addSecurityItem(securityRequirement) 
            .components(new Components()
                    .addSecuritySchemes(securitySchemeName,
                            new SecurityScheme()
                                    .name(securitySchemeName)
                                    .type(SecurityScheme.Type.HTTP)
                                    .scheme("bearer")
                                    .bearerFormat("JWT")
                    )
            );
}
}