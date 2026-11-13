package hiou.hicham.accountservice.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Description de l'API exposee dans Swagger UI (/swagger-ui.html).
 */
@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI accountServiceOpenApi() {
        return new OpenAPI().info(new Info()
                .title("Account Service - CQRS & Event Sourcing")
                .version("1.0.0")
                .description("""
                        Micro-service de gestion de comptes bancaires construit avec Spring Boot,
                        Axon Framework et Axon Server. L'API est separee en deux parties :
                        /commands/accounts pour le cote ecriture et /query/accounts pour le cote lecture.""")
                .contact(new Contact().name("Hicham Ouaouche - Master SDIA, ENSET Mohammedia")));
    }
}
