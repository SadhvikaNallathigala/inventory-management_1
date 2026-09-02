package com.inventory.common;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Every single endpoint in this project returns this exact shape, so the
 * Swagger docs and the frontend only ever have to deal with ONE JSON
 * contract:
 *
 * {
 *   "success": true,
 *   "data": { ... } | [ ... ] | null,
 *   "error": null | { "code": "...", "message": "..." },
 *   "meta":  { ... extra info, e.g. counts, timestamp ... }
 * }
 *
 * [Java basics: Class, Object, Constructor, Methods, Generics, Data Types]
 * `<T>` makes this class generic so the same envelope wraps a single
 * product, a list of products, an order, etc. Static factory methods
 * (success/error) are the only way to build one - the constructor is
 * private, which is Encapsulation applied to object creation itself.
 */
public class ApiResponse<T> {

    private final boolean success;
    private final T data;
    private final ApiError error;
    private final Map<String, Object> meta;

    private ApiResponse(boolean success, T data, ApiError error, Map<String, Object> meta) {
        this.success = success;
        this.data = data;
        this.error = error;
        this.meta = meta;
    }

    public static <T> ApiResponse<T> success(T data) {
        return new ApiResponse<>(true, data, null, new LinkedHashMap<>());
    }

    public static <T> ApiResponse<T> success(T data, Map<String, Object> meta) {
        return new ApiResponse<>(true, data, null, meta == null ? new LinkedHashMap<>() : meta);
    }

    public static <T> ApiResponse<T> error(String code, String message) {
        return new ApiResponse<>(false, null, new ApiError(code, message), new LinkedHashMap<>());
    }

    public boolean isSuccess() {
        return success;
    }

    public T getData() {
        return data;
    }

    public ApiError getError() {
        return error;
    }

    public Map<String, Object> getMeta() {
        return meta;
    }
}
