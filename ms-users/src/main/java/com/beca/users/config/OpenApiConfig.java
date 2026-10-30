package com.beca.users.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI usersOpenApi() {
        return new OpenAPI().info(new Info()
                .title("ms-users API")

                .description("Microserviço de cadastro de Usuarios")
                .version("v1.0.0"));
    }
}
