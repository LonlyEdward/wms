package com.wms.backend.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;
import lombok.Getter;

import java.time.Instant;
import java.util.List;

@Getter
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ApiResponse<T> {

    private final boolean success;
    private final T data;
    private final String message;
    private final String error;
    private final List<FieldError> errors;
    private final Instant timestamp;

    // Success response with data
    public static <T> ApiResponse<T> success(T data) {
        return ApiResponse.<T>builder()
                .success(true)
                .data(data)
                .timestamp(Instant.now())
                .build();
    }

    // Success response with data and a message
    public static <T> ApiResponse<T> success(T data, String message) {
        return ApiResponse.<T>builder()
                .success(true)
                .data(data)
                .message(message)
                .timestamp(Instant.now())
                .build();
    }

    // Error response
    public static <T> ApiResponse<T> error(String error, String message) {
        return ApiResponse.<T>builder()
                .success(false)
                .error(error)
                .message(message)
                .timestamp(Instant.now())
                .build();
    }

    // Validation error response with field level errors
    public static <T> ApiResponse<T> validationError(List<FieldError> errors) {
        return ApiResponse.<T>builder()
                .success(false)
                .error("VALIDATION_ERROR")
                .message("Request validation failed")
                .errors(errors)
                .timestamp(Instant.now())
                .build();
    }

    // Inner record for field level validation errors
    public record FieldError(String field, String message) {}
}