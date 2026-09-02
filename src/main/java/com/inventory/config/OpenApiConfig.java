package com.inventory.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI inventoryOpenAPI() {
        return new OpenAPI().info(new Info()
                .title("Inventory Management API")
                .version("1.0.0")
                .description("Every endpoint returns { success, data, error, meta }. "
                        + "Products: add / edit-or-restock / delete / search-or-display / stock history. "
                        + "Orders: place (Customer or Premium) / search-or-display."));
    }
}
