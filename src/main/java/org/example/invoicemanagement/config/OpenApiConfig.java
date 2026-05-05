package org.example.invoicemanagement.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI invoiceManagementOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Invoice Management API")
                        .description("Spring Boot REST API for managing clients, invoices, invoice items, payments, filters, pagination, and sorting.")
                        .version("v1.0"));
    }
}