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
        LocalDateTime createdAt,
        LocalDateTime completedAt
) {

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
                ride.getCreatedAt(),
                ride.getCompletedAt());
    }
}
