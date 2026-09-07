package com.ridehailing.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import org.springframework.http.HttpStatus;

/**
 * Uniform envelope wrapping every API response (success or failure).
 *
 * <pre>
 * {
 *   "statusCode": 200,
 *   "success": true,
 *   "message": "Operation successful",
 *   "data": { ... },
 *   "error": null
 * }
 * </pre>
 *
 * @param <T> type of the payload carried in {@code data}
 */
@JsonInclude(JsonInclude.Include.ALWAYS)
public class ApiResponse<T> {

    private int statusCode;
    private boolean success;
    private String message;
    private T data;
    private ErrorDetail error;

    private ApiResponse(int statusCode, boolean success, String message, T data, ErrorDetail error) {
        this.statusCode = statusCode;
        this.success = success;
        this.message = message;
        this.data = data;
        this.error = error;
    }

    public static <T> ApiResponse<T> success(HttpStatus status, String message, T data) {
        return new ApiResponse<>(status.value(), true, message, data, null);
    }

    public static <T> ApiResponse<T> success(String message, T data) {
        return success(HttpStatus.OK, message, data);
    }

    public static <T> ApiResponse<T> failure(HttpStatus status, String message, ErrorDetail error) {
        return new ApiResponse<>(status.value(), false, message, null, error);
    }

    // --- getters (needed for Jackson serialization) ---

    public int getStatusCode() {
        return statusCode;
    }

    public boolean isSuccess() {
        return success;
    }

    public String getMessage() {
        return message;
    }

    public T getData() {
        return data;
    }

    public ErrorDetail getError() {
        return error;
    }
}
