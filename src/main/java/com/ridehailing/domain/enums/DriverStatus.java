package com.ridehailing.domain.enums;

/**
 * Driver presence / trip state machine:
 * {@code OFFLINE → AVAILABLE → IN_RIDE_PICKUP → IN_RIDE_ONROUTE → IN_RIDE_ARRIVED → AVAILABLE}.
 * <p>
 * WebSocket connect at {@code /ws/driver/{driverId}} moves a driver to {@link #AVAILABLE};
 * disconnect forces {@link #OFFLINE}.
 */
public enum DriverStatus {
    OFFLINE,
    AVAILABLE,
    IN_RIDE_PICKUP,
    IN_RIDE_ONROUTE,
    IN_RIDE_ARRIVED;

    public boolean isAvailableForMatching() {
        return this == AVAILABLE;
    }

    public boolean isOnTrip() {
        return this == IN_RIDE_PICKUP || this == IN_RIDE_ONROUTE || this == IN_RIDE_ARRIVED;
    }
}
