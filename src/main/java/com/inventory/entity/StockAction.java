package com.inventory.entity;

/**
 * [Java basics: Class-like construct - Enum]
 * A fixed set of the kinds of events that can happen to a product's
 * stock. Used with a `switch` in ProductServiceImpl to decide the
 * human-readable note stored in stock history.
 */
public enum StockAction {
    CREATED,
    STOCK_INCREASED,
    STOCK_DECREASED,
    DETAILS_UPDATED,
    DELETED
}
