package com.ridehailing.domain.enums;

/**
 * Ride lifecycle: {@code REQUESTED → ASSIGNED → ONGOING → COMPLETED} (or {@link #CANCELLED}).
 * A user may have at most one active ride ({@link #REQUESTED}, {@link #ASSIGNED}, or
 * {@link #ONGOING}) at a time. Terminal receipts ({@link #COMPLETED}, {@link #CANCELLED})
 * are treated as immutable by the ride service.
 */
public enum RideStatus {
    REQUESTED,
    ASSIGNED,
    ONGOING,
    COMPLETED,
    CANCELLED;

    public boolean isActive() {
        return this == REQUESTED || this == ASSIGNED || this == ONGOING;
    }

    public boolean isTerminal() {
        return this == COMPLETED || this == CANCELLED;
    }
}
