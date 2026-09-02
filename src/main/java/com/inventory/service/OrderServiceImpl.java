package com.inventory.service;

import com.inventory.common.AppConstants;
import com.inventory.dto.OrderDtos.OrderRequest;
import com.inventory.dto.OrderDtos.OrderResponse;
import com.inventory.entity.CustomerOrder;
import com.inventory.entity.Product;
import com.inventory.entity.StockAction;
import com.inventory.entity.StockHistory;
import com.inventory.exception.AppExceptions.InsufficientStockException;
import com.inventory.exception.AppExceptions.ProductNotFoundException;
import com.inventory.exception.AppExceptions.ValidationFailedException;
import com.inventory.repository.OrderRepository;
import com.inventory.repository.ProductRepository;
import com.inventory.repository.StockHistoryRepository;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.PriorityQueue;
import java.util.Queue;
import java.util.LinkedList;
import java.util.Set;

/**
 * ALL order business logic (both Customer Orders and Premium Orders)
 * lives here. Placing an order also debits the product's stock and
 * writes a stock-history row, so the two features stay consistent.
 */
@Service
public class OrderServiceImpl implements OrderService, AppConstants {

    private final OrderRepository orderRepository;
    private final ProductRepository productRepository;
    private final StockHistoryRepository stockHistoryRepository;

    public OrderServiceImpl(OrderRepository orderRepository, ProductRepository productRepository,
                             StockHistoryRepository stockHistoryRepository) {
        this.orderRepository = orderRepository;
        this.productRepository = productRepository;
        this.stockHistoryRepository = stockHistoryRepository;
    }

    // ---------------------------------------------------------------
    // Customer Orders + Premium Orders
    // ---------------------------------------------------------------
    @Override
    public OrderResponse placeOrder(OrderRequest request) {
        if (request.getCustomerName() == null || request.getCustomerName().isBlank()) {
            throw new ValidationFailedException("Customer name is required");
        }
        if (request.getQuantity() < 1) {
            throw new ValidationFailedException("Quantity must be at least 1");
        }

        String code = request.getProductCode().trim().toUpperCase();
        Product product = productRepository.findByCodeAndDeletedFalse(code)
                .orElseThrow(() -> new ProductNotFoundException(code));

        if (product.getQuantity() < request.getQuantity()) {
            throw new InsufficientStockException(code, product.getQuantity(), request.getQuantity());
        }

        // Ternary operator + Switch-like branching via the `premium` flag:
        // premium orders default to top priority unless a level was given.
        int priorityLevel = request.isPremium()
                ? (request.getPriorityLevel() != null ? request.getPriorityLevel() : PREMIUM_DEFAULT_PRIORITY)
                : REGULAR_DEFAULT_PRIORITY;

        String orderCode = ORDER_CODE_PREFIX + String.format("%06d", orderRepository.count() + 1);

        CustomerOrder order = new CustomerOrder(orderCode, request.getCustomerName().trim(), code,
                product.getName(), request.getQuantity(), request.isPremium(), priorityLevel);
        orderRepository.save(order);

        // Debit stock and keep the audit trail consistent.
        int before = product.getQuantity();
        product.setQuantity(before - request.getQuantity());
        productRepository.save(product);
        stockHistoryRepository.save(new StockHistory(product.getCode(), product.getName(),
                StockAction.STOCK_DECREASED, before, product.getQuantity(),
                "Order " + orderCode + " placed by " + order.getCustomerName()));

        return toResponseWithQueuePosition(order);
    }

    // ---------------------------------------------------------------
    // Search orders + how many orders are placed (Display + Search)
    // ---------------------------------------------------------------
    @Override
    public List<OrderResponse> searchOrders(String customerName, Boolean premium) {
        String clean = (customerName == null || customerName.isBlank()) ? null : customerName.trim();
        List<CustomerOrder> orders = orderRepository.search(clean, premium);

        // Build the "who gets served next" queue across ALL pending
        // orders so every returned order can show its position in line.
        Map<Long, Integer> positionById = computeQueuePositions();

        List<OrderResponse> results = new ArrayList<>();
        for (CustomerOrder o : orders) {
            OrderResponse response = new OrderResponse(o);
            response.setQueuePosition(positionById.getOrDefault(o.getId(), 0));
            results.add(response);
        }
        return results;
    }

    @Override
    public Map<String, Object> getOrderMeta() {
        long total = orderRepository.count();
        long premiumCount = orderRepository.countByPremiumTrue();
        long regularCount = orderRepository.countByPremiumFalse();

        // HashSet: unique customer names -> "how many distinct customers
        // have placed orders" without caring about order.
        Set<String> uniqueCustomers = new HashSet<>();
        // HashMap: customer name -> number of orders they've placed.
        Map<String, Integer> ordersPerCustomer = new HashMap<>();
        for (CustomerOrder o : orderRepository.findAll()) {
            uniqueCustomers.add(o.getCustomerName());
            ordersPerCustomer.merge(o.getCustomerName(), 1, Integer::sum);
        }

        Map<String, Object> meta = new java.util.LinkedHashMap<>();
        meta.put("totalOrders", total);
        meta.put("premiumOrders", premiumCount);
        meta.put("regularOrders", regularCount);
        meta.put("uniqueCustomers", uniqueCustomers.size());
        meta.put("ordersPerCustomer", ordersPerCustomer);
        return meta;
    }

    // ---------------------------------------------------------------
    // Queue position - demonstrates Queue (FIFO) and PriorityQueue
    // ---------------------------------------------------------------
    private Map<Long, Integer> computeQueuePositions() {
        List<CustomerOrder> all = orderRepository.search(null, null);

        // Queue<...> (FIFO) for regular orders - strictly first-arrived,
        // first-served. Backed by a LinkedList, which implements Queue.
        Queue<CustomerOrder> regularQueue = new LinkedList<>();
        // PriorityQueue for premium orders - lower priorityLevel (and,
        // for ties, earlier arrival) is served first.
        PriorityQueue<CustomerOrder> premiumQueue = new PriorityQueue<>(
                Comparator.comparingInt(CustomerOrder::getPriorityLevel)
                        .thenComparing(CustomerOrder::getCreatedAt));

        for (CustomerOrder o : all) {
            if (o.isPremium()) {
                premiumQueue.add(o);
            } else {
                regularQueue.add(o);
            }
        }

        Map<Long, Integer> position = new HashMap<>();
        int rank = 1;
        // While loop draining the PriorityQueue in priority order.
        while (!premiumQueue.isEmpty()) {
            position.put(premiumQueue.poll().getId(), rank);
            rank++;
        }
        // Every premium order is served before any regular one.
        while (!regularQueue.isEmpty()) {
            position.put(regularQueue.poll().getId(), rank);
            rank++;
        }
        return position;
    }

    private OrderResponse toResponseWithQueuePosition(CustomerOrder order) {
        OrderResponse response = new OrderResponse(order);
        response.setQueuePosition(computeQueuePositions().getOrDefault(order.getId(), 0));
        return response;
    }
}
