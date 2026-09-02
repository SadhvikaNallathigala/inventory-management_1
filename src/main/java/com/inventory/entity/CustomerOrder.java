package com.inventory.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;

/**
 * Covers BOTH "Customer Orders" and "Premium Orders" from the feature
 * list with a single table/endpoint - the only real difference between
 * the two is the `premium` flag and its `priorityLevel`, not a separate
 * concept, so they don't need separate classes (see OrderServiceImpl
 * for how premium orders are served ahead of regular ones using a
 * PriorityQueue).
 */
@Entity
@Table(name = "customer_orders")
public class CustomerOrder extends BaseEntity {

    @Column(name = "order_code", nullable = false, unique = true, length = 20)
    private String orderCode;

    @Column(name = "customer_name", nullable = false)
    private String customerName;

    @Column(name = "product_code", nullable = false, length = 20)
    private String productCode;

    @Column(name = "product_name", nullable = false)
    private String productName;

    @Column(name = "quantity", nullable = false)
    private int quantity;

    @Column(name = "premium", nullable = false)
    private boolean premium;

    @Column(name = "priority_level", nullable = false)
    private int priorityLevel;

    public CustomerOrder() {
    }

    public CustomerOrder(String orderCode, String customerName, String productCode, String productName,
                          int quantity, boolean premium, int priorityLevel) {
        this.orderCode = orderCode;
        this.customerName = customerName;
        this.productCode = productCode;
        this.productName = productName;
        this.quantity = quantity;
        this.premium = premium;
        this.priorityLevel = priorityLevel;
    }

    public String getOrderCode() {
        return orderCode;
    }

    public String getCustomerName() {
        return customerName;
    }

    public String getProductCode() {
        return productCode;
    }

    public String getProductName() {
        return productName;
    }

    public int getQuantity() {
        return quantity;
    }

    public boolean isPremium() {
        return premium;
    }

    public int getPriorityLevel() {
        return priorityLevel;
    }
}
