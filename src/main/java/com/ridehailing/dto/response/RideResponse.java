package com.ridehailing.dto.response;

import com.ridehailing.domain.Location;
import com.ridehailing.domain.Ride;
import com.ridehailing.domain.enums.CarType;
import com.ridehailing.domain.enums.RideStatus;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record RideResponse(
        Long rideId,
        Long userId,
        Long driverId,
        Location pickupLocation,
        Location destinationLocation,
        CarType requestedCarType,
        CarType assignedCarType,
        RideStatus status,
        BigDecimal baseFare,
        String appliedCouponCode,
        BigDecimal discountAmount,
        BigDecimal finalPayableFare,
        BigDecimal surgeMultiplier,
        LocalDateTime createdAt,
        LocalDateTime completedAt,
        LocalDateTime cancelledAt,
        BigDecimal cancellationFee,
        String cancellationReason
) {

    public RideResponse(Long rideId, Long userId, Long driverId, Location pickupLocation,
                        Location destinationLocation, CarType requestedCarType,
                        CarType assignedCarType, RideStatus status, BigDecimal baseFare,
                        String appliedCouponCode, BigDecimal discountAmount,
                        BigDecimal finalPayableFare, LocalDateTime createdAt,
                        LocalDateTime completedAt) {
        this(rideId, userId, driverId, pickupLocation, destinationLocation, requestedCarType,
                assignedCarType, status, baseFare, appliedCouponCode, discountAmount,
                finalPayableFare, BigDecimal.ONE.setScale(2), createdAt, completedAt,
                null, BigDecimal.ZERO.setScale(2), null);
    }

    public static RideResponse from(Ride ride) {
        return new RideResponse(
                ride.getRideId(),
                ride.getUserId(),
                ride.getDriverId(),
                ride.getPickupLocation(),
                ride.getDestinationLocation(),
                ride.getRequestedCarType(),
                ride.getAssignedCarType(),
                ride.getStatus(),
                ride.getBaseFare(),
                ride.getAppliedCouponCode(),
                ride.getDiscountAmount(),
                ride.getFinalPayableFare(),
                ride.getSurgeMultiplier(),
                ride.getCreatedAt(),
                ride.getCompletedAt(), ride.getCancelledAt(), ride.getCancellationFee(),
                ride.getCancellationReason());
    }
}
