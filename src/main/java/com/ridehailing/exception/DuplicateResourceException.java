package com.ridehailing.exception;

import org.springframework.http.HttpStatus;

/**
 * Thrown when a create operation violates a uniqueness constraint (phone number,
 * vehicle registration number, coupon code). Maps to HTTP 400 -- not 409 -- to stay within
 * the 200/400/404 contract mandated for this service.
 */
public class DuplicateResourceException extends RideHailingException {

    public DuplicateResourceException(String resourceName, String field, Object value) {
        super(HttpStatus.BAD_REQUEST, "DUPLICATE_RESOURCE",
                "%s with %s '%s' already exists".formatted(resourceName, field, value));
    }
}
