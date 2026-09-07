package com.ridehailing.exception;

import org.springframework.http.HttpStatus;

/**
 * Base type for all domain exceptions. Carries the HTTP status the {@code GlobalExceptionHandler}
 * should translate this into, plus a stable machine-readable error code for API consumers.
 * <p>
 * Per the API contract, only HTTP 200/400/404 are ever returned by this service, so every
 * subclass must resolve to {@link HttpStatus#BAD_REQUEST} or {@link HttpStatus#NOT_FOUND}.
 */
public abstract class RideHailingException extends RuntimeException {

    private final HttpStatus httpStatus;
    private final String errorCode;

    protected RideHailingException(HttpStatus httpStatus, String errorCode, String message) {
        super(message);
        this.httpStatus = httpStatus;
        this.errorCode = errorCode;
    }

    public HttpStatus getHttpStatus() {
        return httpStatus;
    }

    public String getErrorCode() {
        return errorCode;
    }
}
