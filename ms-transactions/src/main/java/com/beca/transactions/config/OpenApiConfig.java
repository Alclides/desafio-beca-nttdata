package com.beca.transactions.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI transactionsOpenApi() {
        return new OpenAPI().info(new Info()
                .title("ms-transactions API")
                .description("Microserviço de transações financeiras: registro, aprovação por saldo e limites, e consulta")
                .version("v1.0.0"));
    }
}
