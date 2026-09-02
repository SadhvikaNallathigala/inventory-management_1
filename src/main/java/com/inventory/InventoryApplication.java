package com.inventory;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Boot entry point only - no business logic lives here.
 *
 * Once running:
 *  - Website (Products page)  -> http://localhost:8080/index.html
 *  - Website (Orders page)    -> http://localhost:8080/orders.html
 *  - Swagger UI               -> http://localhost:8080/swagger-ui.html
 */
@SpringBootApplication
public class InventoryApplication {
    public static void main(String[] args) {
        SpringApplication.run(InventoryApplication.class, args);
    }
}
