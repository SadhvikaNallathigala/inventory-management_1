package com.inventory.dto;

import com.inventory.entity.CustomerOrder;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;

import java.time.LocalDateTime;

/**
 * All Order-related request/response shapes, covering both Customer
 * Orders and Premium Orders (the `premium` flag is the only thing that
 * tells them apart).
 */
public final class OrderDtos {

    private OrderDtos() {
    }

    /** Body for POST /api/orders (Customer Orders + Premium Orders). */
    public static class OrderRequest {
        @NotBlank(message = "Customer name is required")
        private String customerName;

        @NotBlank(message = "Product code is required")
        private String productCode;

        @Min(value = 1, message = "Quantity must be at least 1")
        private int quantity;

        private boolean premium;

        /** 1 = highest priority. Only meaningful when premium = true. */
        private Integer priorityLevel;

        public String getCustomerName() { return customerName; }
        public void setCustomerName(String customerName) { this.customerName = customerName; }
        public String getProductCode() { return productCode; }
        public void setProductCode(String productCode) { this.productCode = productCode; }
        public int getQuantity() { return quantity; }
        public void setQuantity(int quantity) { this.quantity = quantity; }
        public boolean isPremium() { return premium; }
        public void setPremium(boolean premium) { this.premium = premium; }
        public Integer getPriorityLevel() { return priorityLevel; }
        public void setPriorityLevel(Integer priorityLevel) { this.priorityLevel = priorityLevel; }
    }

    /** What every order-facing endpoint returns. `queuePosition` shows
     *  where this order sits in the "who gets served next" line -
     *  premium orders (by priority) always come before regular ones
     *  (FIFO) - see OrderServiceImpl for how it's computed. */
    public static class OrderResponse {
        private final String orderCode;
        private final String customerName;
        private final String productCode;
        private final String productName;
        private final int quantity;
        private final boolean premium;
        private final int priorityLevel;
        private final LocalDateTime placedAt;
        private int queuePosition;

        public OrderResponse(CustomerOrder o) {
            this.orderCode = o.getOrderCode();
            this.customerName = o.getCustomerName();
            this.productCode = o.getProductCode();
            this.productName = o.getProductName();
            this.quantity = o.getQuantity();
            this.premium = o.isPremium();
            this.priorityLevel = o.getPriorityLevel();
            this.placedAt = o.getCreatedAt();
        }

        public String getOrderCode() { return orderCode; }
        public String getCustomerName() { return customerName; }
        public String getProductCode() { return productCode; }
        public String getProductName() { return productName; }
        public int getQuantity() { return quantity; }
        public boolean isPremium() { return premium; }
        public int getPriorityLevel() { return priorityLevel; }
        public LocalDateTime getPlacedAt() { return placedAt; }
        public int getQueuePosition() { return queuePosition; }
        public void setQueuePosition(int queuePosition) { this.queuePosition = queuePosition; }
    }
}
