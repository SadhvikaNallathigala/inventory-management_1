package com.inventory.service;

import com.inventory.dto.OrderDtos.OrderRequest;
import com.inventory.dto.OrderDtos.OrderResponse;

import java.util.List;
import java.util.Map;

public interface OrderService {

    OrderResponse placeOrder(OrderRequest request);

    List<OrderResponse> searchOrders(String customerName, Boolean premium);

    /** Counts for the Orders page stat cards. */
    Map<String, Object> getOrderMeta();
}
