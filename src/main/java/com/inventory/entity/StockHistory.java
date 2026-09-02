package com.inventory.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;

/**
 * One permanent row per stock event (add, restock, sell-down, edit,
 * delete). Rows are keyed by productCode (not a foreign key to
 * Product's id) on purpose: even after a product is soft-deleted, its
 * full history keeps existing in Postgres exactly as the mentor asked.
 */
@Entity
@Table(name = "stock_history")
public class StockHistory extends BaseEntity {

    @Column(name = "product_code", nullable = false, length = 20)
    private String productCode;

    @Column(name = "product_name", nullable = false)
    private String productName;

    @Enumerated(EnumType.STRING)
    @Column(name = "action", nullable = false, length = 30)
    private StockAction action;

    @Column(name = "previous_quantity", nullable = false)
    private int previousQuantity;

    @Column(name = "new_quantity", nullable = false)
    private int newQuantity;

    @Column(name = "change_amount", nullable = false)
    private int changeAmount;

    @Column(name = "note")
    private String note;

    public StockHistory() {
    }

    public StockHistory(String productCode, String productName, StockAction action,
                         int previousQuantity, int newQuantity, String note) {
        this.productCode = productCode;
        this.productName = productName;
        this.action = action;
        this.previousQuantity = previousQuantity;
        this.newQuantity = newQuantity;
        // Operators: arithmetic subtraction used to derive the change amount.
        this.changeAmount = newQuantity - previousQuantity;
        this.note = note;
    }

    public String getProductCode() {
        return productCode;
    }

    public String getProductName() {
        return productName;
    }

    public StockAction getAction() {
        return action;
    }

    public int getPreviousQuantity() {
        return previousQuantity;
    }

    public int getNewQuantity() {
        return newQuantity;
    }

    public int getChangeAmount() {
        return changeAmount;
    }

    public String getNote() {
        return note;
    }
}
