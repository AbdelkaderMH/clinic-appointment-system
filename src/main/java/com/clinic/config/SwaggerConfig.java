package com.clinic.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Configuration for OpenAPI/Swagger documentation.  Exposes a custom
 * {@link OpenAPI} bean describing the clinic appointment API.
 */
@Configuration
public class SwaggerConfig {

    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .info(new Info().title("Clinic Appointment API")
                .version("1.0.0")
                .description("API documentation for the Clinic Appointment System")
                .license(new License().name("MIT").url("https://opensource.org/licenses/MIT")));
    }
}