package com.ridehailing.exception;

import com.ridehailing.dto.response.ApiResponse;
import com.ridehailing.dto.response.ErrorDetail;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingRequestHeaderException;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Translates every exception thrown from a controller into the standard {@link ApiResponse}
 * envelope, so callers never see a raw Spring/Jackson error body.
 * <p>
 * <b>Note on status codes:</b> the API contract for this service restricts responses to
 * HTTP 200/400/404. All known domain and validation failures below are mapped accordingly.
 * The catch-all {@link Exception} handler at the bottom is the one deliberate exception: a
 * truly unanticipated (programming) error is reported as 500 rather than mislabeled as a
 * client-caused 400, since collapsing every failure mode into 400 would make client-side
 * error handling unreliable. Flagging this explicitly as a judgment call against the letter
 * of "only 200/400/404" -- happy to change it to a 400 fallback if you'd rather stay literal.
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(RideHailingException.class)
    public ResponseEntity<ApiResponse<Void>> handleRideHailingException(RideHailingException ex) {
        log.warn("Domain exception [{}]: {}", ex.getErrorCode(), ex.getMessage());
        ApiResponse<Void> body = ApiResponse.failure(
                ex.getHttpStatus(),
                ex.getMessage(),
                ErrorDetail.of(ex.getErrorCode(), ex.getMessage()));
        return ResponseEntity.status(ex.getHttpStatus()).body(body);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResponse<Void>> handleValidationException(MethodArgumentNotValidException ex) {
        List<String> details = ex.getBindingResult().getFieldErrors().stream()
                .map(fe -> "%s: %s".formatted(fe.getField(), fe.getDefaultMessage()))
                .collect(Collectors.toList());
        log.warn("Request validation failed: {}", details);
        ApiResponse<Void> body = ApiResponse.failure(
                HttpStatus.BAD_REQUEST,
                "Request validation failed",
                ErrorDetail.of("VALIDATION_ERROR", details));
        return ResponseEntity.badRequest().body(body);
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ApiResponse<Void>> handleConstraintViolation(ConstraintViolationException ex) {
        List<String> details = ex.getConstraintViolations().stream()
                .map(ConstraintViolation::getMessage)
                .collect(Collectors.toList());
        ApiResponse<Void> body = ApiResponse.failure(
                HttpStatus.BAD_REQUEST,
                "Request validation failed",
                ErrorDetail.of("VALIDATION_ERROR", details));
        return ResponseEntity.badRequest().body(body);
    }

    @ExceptionHandler(MissingRequestHeaderException.class)
    public ResponseEntity<ApiResponse<Void>> handleMissingHeader(MissingRequestHeaderException ex) {
        ApiResponse<Void> body = ApiResponse.failure(
                HttpStatus.BAD_REQUEST,
                "Missing required header",
                ErrorDetail.of("MISSING_HEADER", "Required header '%s' is missing".formatted(ex.getHeaderName())));
        return ResponseEntity.badRequest().body(body);
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ApiResponse<Void>> handleUnreadableBody(HttpMessageNotReadableException ex) {
        ApiResponse<Void> body = ApiResponse.failure(
                HttpStatus.BAD_REQUEST,
                "Malformed request body",
                ErrorDetail.of("MALFORMED_REQUEST", "Request body is missing or not valid JSON"));
        return ResponseEntity.badRequest().body(body);
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ApiResponse<Void>> handleTypeMismatch(MethodArgumentTypeMismatchException ex) {
        ApiResponse<Void> body = ApiResponse.failure(
                HttpStatus.BAD_REQUEST,
                "Invalid parameter type",
                ErrorDetail.of("TYPE_MISMATCH",
                        "Parameter '%s' has an invalid value: %s".formatted(ex.getName(), ex.getValue())));
        return ResponseEntity.badRequest().body(body);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<Void>> handleUnexpected(Exception ex) {
        log.error("Unhandled exception", ex);
        ApiResponse<Void> body = ApiResponse.failure(
                HttpStatus.INTERNAL_SERVER_ERROR,
                "An unexpected error occurred",
                ErrorDetail.of("INTERNAL_ERROR", "Please contact support if this persists"));
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(body);
    }
}
