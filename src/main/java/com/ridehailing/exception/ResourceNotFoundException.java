package com.ridehailing.exception;

import org.springframework.http.HttpStatus;

/**
 * Thrown when a lookup by id/code (User, Driver, Ride, Coupon) finds nothing. Maps to HTTP 404.
 */
public class ResourceNotFoundException extends RideHailingException {

    public ResourceNotFoundException(String resourceName, Object identifier) {
        super(HttpStatus.NOT_FOUND, "RESOURCE_NOT_FOUND",
                "%s not found for identifier: %s".formatted(resourceName, identifier));
    }
}
