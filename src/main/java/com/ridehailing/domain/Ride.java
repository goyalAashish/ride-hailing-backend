package com.ridehailing.domain;

import com.ridehailing.domain.enums.CarType;
import com.ridehailing.domain.enums.RideStatus;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Objects;

/**
 * A single trip request and its fare snapshot. Financial fields use {@link BigDecimal};
 * scale and rounding are applied by the pricing engine, not by this entity.
 */
public class Ride {

    private Long rideId;
    private Long userId;
    private Long driverId;
    private Location pickupLocation;
    private Location destinationLocation;
    private CarType requestedCarType;
    private CarType assignedCarType;
    private RideStatus status = RideStatus.REQUESTED;
    private BigDecimal baseFare;
    private String appliedCouponCode;
    private BigDecimal discountAmount;
    private BigDecimal finalPayableFare;
    private BigDecimal surgeMultiplier = BigDecimal.ONE.setScale(2);
    private LocalDateTime createdAt;
    private LocalDateTime completedAt;
    private LocalDateTime cancelledAt;
    private BigDecimal cancellationFee = BigDecimal.ZERO.setScale(2);
    private String cancellationReason;

    public Ride() {
    }

    public Long getRideId() {
        return rideId;
    }

    public void setRideId(Long rideId) {
        this.rideId = rideId;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public Long getDriverId() {
        return driverId;
    }

    public void setDriverId(Long driverId) {
        this.driverId = driverId;
    }

    public Location getPickupLocation() {
        return pickupLocation;
    }

    public void setPickupLocation(Location pickupLocation) {
        this.pickupLocation = pickupLocation;
    }

    public Location getDestinationLocation() {
        return destinationLocation;
    }

    public void setDestinationLocation(Location destinationLocation) {
        this.destinationLocation = destinationLocation;
    }

    public CarType getRequestedCarType() {
        return requestedCarType;
    }

    public void setRequestedCarType(CarType requestedCarType) {
        this.requestedCarType = requestedCarType;
    }

    public CarType getAssignedCarType() {
        return assignedCarType;
    }

    public void setAssignedCarType(CarType assignedCarType) {
        this.assignedCarType = assignedCarType;
    }

    public RideStatus getStatus() {
        return status;
    }

    public void setStatus(RideStatus status) {
        this.status = status;
    }

    public BigDecimal getBaseFare() {
        return baseFare;
    }

    public void setBaseFare(BigDecimal baseFare) {
        this.baseFare = baseFare;
    }

    public String getAppliedCouponCode() {
        return appliedCouponCode;
    }

    public void setAppliedCouponCode(String appliedCouponCode) {
        this.appliedCouponCode = appliedCouponCode;
    }

    public BigDecimal getDiscountAmount() {
        return discountAmount;
    }

    public void setDiscountAmount(BigDecimal discountAmount) {
        this.discountAmount = discountAmount;
    }

    public BigDecimal getFinalPayableFare() {
        return finalPayableFare;
    }

    public void setFinalPayableFare(BigDecimal finalPayableFare) {
        this.finalPayableFare = finalPayableFare;
    }

    public BigDecimal getSurgeMultiplier() {
        return surgeMultiplier;
    }

    public void setSurgeMultiplier(BigDecimal surgeMultiplier) {
        this.surgeMultiplier = surgeMultiplier;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getCompletedAt() {
        return completedAt;
    }

    public void setCompletedAt(LocalDateTime completedAt) {
        this.completedAt = completedAt;
    }

    public LocalDateTime getCancelledAt() {
        return cancelledAt;
    }

    public void setCancelledAt(LocalDateTime cancelledAt) {
        this.cancelledAt = cancelledAt;
    }

    public BigDecimal getCancellationFee() {
        return cancellationFee;
    }

    public void setCancellationFee(BigDecimal cancellationFee) {
        this.cancellationFee = cancellationFee;
    }

    public String getCancellationReason() {
        return cancellationReason;
    }

    public void setCancellationReason(String cancellationReason) {
        this.cancellationReason = cancellationReason;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof Ride ride) || rideId == null || ride.rideId == null) {
            return false;
        }
        return rideId.equals(ride.rideId);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(rideId);
    }
}
