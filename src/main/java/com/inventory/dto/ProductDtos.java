package com.inventory.dto;

import com.inventory.entity.Product;
import com.inventory.entity.StockAction;
import com.inventory.entity.StockHistory;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.PositiveOrZero;

import java.time.LocalDateTime;

/**
 * All Product-related request/response shapes live in this one file as
 * small nested static classes - keeps the DTO layer to a handful of
 * files instead of one file per shape.
 *
 * [Java basics: Class, Object, Constructor, Encapsulation]
 */
public final class ProductDtos {

    private ProductDtos() {
        // utility holder class, never instantiated
    }

    /** Body for POST /api/products (Add Product). */
    public static class ProductRequest {
        @NotBlank(message = "Product code is required")
        private String code;

        @NotBlank(message = "Product name is required")
        private String name;

        @NotBlank(message = "Category is required")
        private String category;

        @PositiveOrZero(message = "Price cannot be negative")
        private double price;

        @PositiveOrZero(message = "Quantity cannot be negative")
        private int quantity;

        public String getCode() { return code; }
        public void setCode(String code) { this.code = code; }
        public String getName() { return name; }
        public void setName(String name) { this.name = name; }
        public String getCategory() { return category; }
        public void setCategory(String category) { this.category = category; }
        public double getPrice() { return price; }
        public void setPrice(double price) { this.price = price; }
        public int getQuantity() { return quantity; }
        public void setQuantity(int quantity) { this.quantity = quantity; }
    }

    /**
     * Body for PUT /api/products/{code} - the single "edit" endpoint
     * behind the pen icon. Every field is optional: send only what
     * changed. `quantityChange` is a signed delta (+10 to restock,
     * -3 to sell down) which is how "Update Stock" is folded into the
     * same edit action instead of needing its own endpoint.
     */
    public static class ProductUpdateRequest {
        private String name;
        private String category;
        private Double price;
        private Integer quantityChange;
        private String reason;

        public String getName() { return name; }
        public void setName(String name) { this.name = name; }
        public String getCategory() { return category; }
        public void setCategory(String category) { this.category = category; }
        public Double getPrice() { return price; }
        public void setPrice(Double price) { this.price = price; }
        public Integer getQuantityChange() { return quantityChange; }
        public void setQuantityChange(Integer quantityChange) { this.quantityChange = quantityChange; }
        public String getReason() { return reason; }
        public void setReason(String reason) { this.reason = reason; }
    }

    /** What every product-facing endpoint returns. */
    public static class ProductResponse {
        private final String code;
        private final String name;
        private final String category;
        private final double price;
        private final int quantity;
        private final boolean deleted;
        private final boolean lowStock;
        private final LocalDateTime updatedAt;

        public ProductResponse(Product p) {
            this.code = p.getCode();
            this.name = p.getName();
            this.category = p.getCategory();
            this.price = p.getPrice();
            this.quantity = p.getQuantity();
            this.deleted = p.isDeleted();
            // Operators: relational comparison used to derive a UI badge.
            this.lowStock = !p.isDeleted() && p.getQuantity() < com.inventory.common.AppConstants.LOW_STOCK_THRESHOLD;
            this.updatedAt = p.getUpdatedAt();
        }

        public String getCode() { return code; }
        public String getName() { return name; }
        public String getCategory() { return category; }
        public double getPrice() { return price; }
        public int getQuantity() { return quantity; }
        public boolean isDeleted() { return deleted; }
        public boolean isLowStock() { return lowStock; }
        public LocalDateTime getUpdatedAt() { return updatedAt; }
    }

    /** One row of a product's permanent stock history log. */
    public static class StockHistoryResponse {
        private final StockAction action;
        private final int previousQuantity;
        private final int newQuantity;
        private final int changeAmount;
        private final String note;
        private final LocalDateTime when;

        public StockHistoryResponse(StockHistory h) {
            this.action = h.getAction();
            this.previousQuantity = h.getPreviousQuantity();
            this.newQuantity = h.getNewQuantity();
            this.changeAmount = h.getChangeAmount();
            this.note = h.getNote();
            this.when = h.getCreatedAt();
        }

        public StockAction getAction() { return action; }
        public int getPreviousQuantity() { return previousQuantity; }
        public int getNewQuantity() { return newQuantity; }
        public int getChangeAmount() { return changeAmount; }
        public String getNote() { return note; }
        public LocalDateTime getWhen() { return when; }
    }
}
