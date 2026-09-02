package com.inventory.exception;

/**
 * All custom exceptions for the project live here together (they're
 * one-liners each) so the exception layer doesn't sprawl into many
 * tiny files. GlobalExceptionHandler turns every one of these into
 * the standard error { code, message } shape.
 *
 * [Java basics: Inheritance, Polymorphism]
 * Every exception below IS-A ApiException IS-A RuntimeException -
 * GlobalExceptionHandler catches them polymorphically by their common
 * parent type.
 */
public class AppExceptions {

    private AppExceptions() {
    }

    /** Common parent so the handler can catch every app-specific error at once. */
    public static class ApiException extends RuntimeException {
        private final String code;

        public ApiException(String code, String message) {
            super(message);
            this.code = code;
        }

        public String getCode() {
            return code;
        }
    }

    public static class ProductNotFoundException extends ApiException {
        public ProductNotFoundException(String productCode) {
            super("PRODUCT_NOT_FOUND", "No product found with code: " + productCode);
        }
    }

    public static class DuplicateProductCodeException extends ApiException {
        public DuplicateProductCodeException(String productCode) {
            super("DUPLICATE_PRODUCT_CODE", "Product code already exists: " + productCode);
        }
    }

    public static class InvalidProductCodeException extends ApiException {
        public InvalidProductCodeException(String message) {
            super("INVALID_PRODUCT_CODE", message);
        }
    }

    public static class InsufficientStockException extends ApiException {
        public InsufficientStockException(String productCode, int available, int requested) {
            super("INSUFFICIENT_STOCK", "Product " + productCode + " only has " + available
                    + " in stock, requested " + requested);
        }
    }

    public static class ValidationFailedException extends ApiException {
        public ValidationFailedException(String message) {
            super("VALIDATION_FAILED", message);
        }
    }
}
