package com.inventory.exception;

import com.inventory.common.ApiResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.stream.Collectors;

/**
 * One place that guarantees EVERY error response - validation failure,
 * business rule violation, or unexpected bug - comes back in the same
 * { success:false, data:null, error:{code,message}, meta:{} } shape.
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(AppExceptions.ApiException.class)
    public ResponseEntity<ApiResponse<Object>> handleApiException(AppExceptions.ApiException ex) {
        HttpStatus status = switch (ex.getCode()) {
            case "PRODUCT_NOT_FOUND" -> HttpStatus.NOT_FOUND;
            case "DUPLICATE_PRODUCT_CODE" -> HttpStatus.CONFLICT;
            case "INVALID_PRODUCT_CODE", "INSUFFICIENT_STOCK", "VALIDATION_FAILED" -> HttpStatus.BAD_REQUEST;
            default -> HttpStatus.BAD_REQUEST;
        };
        return ResponseEntity.status(status).body(ApiResponse.error(ex.getCode(), ex.getMessage()));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResponse<Object>> handleValidation(MethodArgumentNotValidException ex) {
        String message = ex.getBindingResult().getFieldErrors().stream()
                .map(fe -> fe.getField() + ": " + fe.getDefaultMessage())
                .collect(Collectors.joining("; "));
        return ResponseEntity.badRequest().body(ApiResponse.error("VALIDATION_FAILED", message));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<Object>> handleUnexpected(Exception ex) {
        return ResponseEntity.internalServerError()
                .body(ApiResponse.error("INTERNAL_ERROR", "Something went wrong: " + ex.getMessage()));
    }
}
