package com.ridehailing.domain;

import com.ridehailing.domain.enums.CarType;

/**
 * Vehicle owned by exactly one {@link Driver} (1:1). {@code registrationNumber} is unique.
 */
public class Vehicle {

    private String registrationNumber;
    private CarType carType;
    private Long driverId;

    public Vehicle() {
    }

    public Vehicle(String registrationNumber, CarType carType) {
        this.registrationNumber = registrationNumber;
        this.carType = carType;
    }

    public String getRegistrationNumber() {
        return registrationNumber;
    }

    public void setRegistrationNumber(String registrationNumber) {
        this.registrationNumber = registrationNumber;
    }

    public CarType getCarType() {
        return carType;
    }

    public void setCarType(CarType carType) {
        this.carType = carType;
    }

    public Long getDriverId() {
        return driverId;
    }

    public void setDriverId(Long driverId) {
        this.driverId = driverId;
    }
}
