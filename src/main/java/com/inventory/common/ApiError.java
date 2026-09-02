package com.inventory.common;

/**
 * The "error" portion of every API response. Null whenever a call
 * succeeds - see {@link ApiResponse}.
 *
 * [Java basics: Class, Object, Constructor, Encapsulation]
 * Plain class with private fields, a constructor, and getters only
 * (no setters - once an error is built it never changes).
 */
public class ApiError {

    private final String code;
    private final String message;

    public ApiError(String code, String message) {
        this.code = code;
        this.message = message;
    }

    public String getCode() {
        return code;
    }

    public String getMessage() {
        return message;
    }
}
