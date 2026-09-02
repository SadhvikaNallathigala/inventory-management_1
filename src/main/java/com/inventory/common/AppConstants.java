package com.inventory.common;

/**
 * [Java basics: Interface Variables]
 * Every field in an interface is implicitly `public static final` -
 * these are shared, read-only constants used across the service layer.
 * No object of this interface is ever created; it exists purely to
 * hold shared values (a common, idiomatic use of interfaces in Java).
 */
public interface AppConstants {

    // Product codes must look like PRD-1001 (PRD- followed by 4 digits).
    String PRODUCT_CODE_REGEX = "^PRD-\\d{4}$";
    String PRODUCT_CODE_HINT = "Expected format PRD-#### e.g. PRD-1001";

    String ORDER_CODE_PREFIX = "ORD-";

    // Premium orders default to top priority (lower number = served sooner);
    // regular orders sit behind every premium order by default.
    int PREMIUM_DEFAULT_PRIORITY = 1;
    int REGULAR_DEFAULT_PRIORITY = 10;

    int LOW_STOCK_THRESHOLD = 5;
}
