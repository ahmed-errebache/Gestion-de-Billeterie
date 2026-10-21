package fr.iut.billetterie.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI billetterieOpenApi() {
        return new OpenAPI().info(new Info()
                .title("Billetterie de concerts")
                .version("1.0")
                .description("API de la billetterie : concerts, clients, carte de fidélité et achats de billets."));
    }
}
