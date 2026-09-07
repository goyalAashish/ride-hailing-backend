package com.ridehailing.domain;

import com.ridehailing.domain.enums.CarType;
import com.ridehailing.domain.enums.DriverStatus;

import java.util.Objects;

/**
 * Driver account with a 1:1 {@link Vehicle}. Phone and registration number are unique.
 * {@link #currentLocation} is populated once the driver is online and is used by matching.
 */
public class Driver {

    private Long driverId;
    private String name;
    private String phone;
    private Vehicle vehicle;
    private DriverStatus status = DriverStatus.OFFLINE;
    private Location currentLocation;
    private double rating = 5.0;
    private int ratingCount;

    public Driver() {
    }

    public Driver(String name, String phone, Vehicle vehicle) {
        this.name = name;
        this.phone = phone;
        this.vehicle = vehicle;
        this.status = DriverStatus.OFFLINE;
    }

    public Long getDriverId() {
        return driverId;
    }

    public void setDriverId(Long driverId) {
        this.driverId = driverId;
        if (vehicle != null) {
            vehicle.setDriverId(driverId);
        }
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public Vehicle getVehicle() {
        return vehicle;
    }

    public void setVehicle(Vehicle vehicle) {
        this.vehicle = vehicle;
        if (vehicle != null && driverId != null) {
            vehicle.setDriverId(driverId);
        }
    }

    public String getRegistrationNumber() {
        return vehicle == null ? null : vehicle.getRegistrationNumber();
    }

    public CarType getCarType() {
        return vehicle == null ? null : vehicle.getCarType();
    }

    public DriverStatus getStatus() {
        return status;
    }

    public void setStatus(DriverStatus status) {
        this.status = status;
    }

    public Location getCurrentLocation() {
        return currentLocation;
    }

    public void setCurrentLocation(Location currentLocation) {
        this.currentLocation = currentLocation;
    }

    public synchronized double getRating() {
        return rating;
    }

    public synchronized int getRatingCount() {
        return ratingCount;
    }

    public synchronized double getAverageRating() {
        return rating;
    }

    public synchronized void setRating(double rating) {
        if (!Double.isFinite(rating) || rating < 1.0 || rating > 5.0) {
            throw new IllegalArgumentException("rating must be between 1 and 5");
        }
        this.rating = rating;
    }

    public synchronized void addRating(double value) {
        if (!Double.isFinite(value) || value < 1.0 || value > 5.0) {
            throw new IllegalArgumentException("rating must be between 1 and 5");
        }
        rating = ((rating * ratingCount) + value) / (++ratingCount);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof Driver driver) || driverId == null || driver.driverId == null) {
            return false;
        }
        return driverId.equals(driver.driverId);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(driverId);
    }
}
