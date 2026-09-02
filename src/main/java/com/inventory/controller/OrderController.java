package com.inventory.controller;

import com.inventory.common.ApiResponse;
import com.inventory.dto.OrderDtos.OrderRequest;
import com.inventory.dto.OrderDtos.OrderResponse;
import com.inventory.service.OrderService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

/**
 * Two endpoints cover both "Customer Orders" and "Premium Orders" -
 * placing one and searching/listing them (which also answers "how many
 * orders have been placed" via the response meta).
 */
@RestController
@RequestMapping("/api/orders")
@Tag(name = "Orders", description = "Place and search Customer Orders and Premium Orders")
public class OrderController {

    private final OrderService orderService;

    public OrderController(OrderService orderService) {
        this.orderService = orderService;
    }

    @PostMapping
    @Operation(summary = "Place Order", description = "Places a Customer Order, or a Premium Order when premium=true.")
    public ResponseEntity<ApiResponse<OrderResponse>> placeOrder(@Valid @RequestBody OrderRequest request) {
        return ResponseEntity.ok(ApiResponse.success(orderService.placeOrder(request)));
    }

    @GetMapping
    @Operation(summary = "Search / Display Orders", description = "Filter by customer name and/or premium flag; meta includes total/premium/regular counts.")
    public ResponseEntity<ApiResponse<List<OrderResponse>>> searchOrders(
            @RequestParam(required = false) String customer,
            @RequestParam(required = false) Boolean premium) {
        List<OrderResponse> orders = orderService.searchOrders(customer, premium);
        Map<String, Object> meta = orderService.getOrderMeta();
        return ResponseEntity.ok(ApiResponse.success(orders, meta));
    }
}
