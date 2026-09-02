package com.inventory.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;

import java.time.LocalDateTime;

/**
 * [Java basics: Class, Object, Constructor, Encapsulation, Inheritance,
 *  Data Types]
 *
 * Extends BaseEntity (Inheritance) to pick up id/createdAt/updatedAt for
 * free. Fields are private with public getters/setters only
 * (Encapsulation) - nothing outside this class can put a Product into an
 * invalid state directly; that job belongs to ProductServiceImpl.
 *
 * `deleted` is a soft-delete flag on purpose: the mentor's requirement
 * is that even a "deleted" product's row must still exist in Postgres
 * (for its stock history to make sense), so DELETE never issues a SQL
 * DELETE - it just flips this flag and stamps deletedAt.
 */
@Entity
@Table(name = "products")
public class Product extends BaseEntity {

    @Column(name = "code", nullable = false, unique = true, length = 20)
    private String code;

    @Column(name = "name", nullable = false)
    private String name;

    @Column(name = "category", nullable = false)
    private String category;

    @Column(name = "price", nullable = false)
    private double price;

    @Column(name = "quantity", nullable = false)
    private int quantity;

    @Column(name = "deleted", nullable = false)
    private boolean deleted = false;

    @Column(name = "deleted_at")
    private LocalDateTime deletedAt;

    public Product() {
    }

    public Product(String code, String name, String category, double price, int quantity) {
        this.code = code;
        this.name = name;
        this.category = category;
        this.price = price;
        this.quantity = quantity;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public double getPrice() {
        return price;
    }

    public void setPrice(double price) {
        this.price = price;
    }

    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }

    public boolean isDeleted() {
        return deleted;
    }

    public void setDeleted(boolean deleted) {
        this.deleted = deleted;
    }

    public LocalDateTime getDeletedAt() {
        return deletedAt;
    }

    public void setDeletedAt(LocalDateTime deletedAt) {
        this.deletedAt = deletedAt;
    }
}
