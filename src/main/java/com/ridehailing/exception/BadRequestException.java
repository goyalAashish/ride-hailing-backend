package com.ridehailing.exception;

import org.springframework.http.HttpStatus;

/**
 * Generic 400 for business-rule violations that don't warrant their own exception type
 * (e.g. malformed request combinations). Prefer a dedicated subclass of
 * {@link RideHailingException} when the failure is a recurring, well-named domain rule
 * (see {@link DuplicateResourceException} for an example) so callers get a stable error code.
 */
public class BadRequestException extends RideHailingException {

    public BadRequestException(String errorCode, String message) {
        super(HttpStatus.BAD_REQUEST, errorCode, message);
    }

    public BadRequestException(String message) {
        this("BAD_REQUEST", message);
    }
}
