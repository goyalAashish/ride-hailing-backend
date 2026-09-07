package com.ridehailing.dto.response;

import com.ridehailing.domain.Driver;
import com.ridehailing.domain.enums.CarType;
import com.ridehailing.domain.enums.DriverStatus;

public record DriverResponse(
        Long driverId,
        String name,
        String phone,
        String registrationNumber,
        CarType carType,
        DriverStatus status,
        double rating,
        int ratingCount
) {

    public DriverResponse(Long driverId, String name, String phone, String registrationNumber,
                          CarType carType, DriverStatus status) {
        this(driverId, name, phone, registrationNumber, carType, status, 5.0, 0);
    }

    public static DriverResponse from(Driver driver) {
        return new DriverResponse(
                driver.getDriverId(),
                driver.getName(),
                driver.getPhone(),
                driver.getRegistrationNumber(),
                driver.getCarType(),
                driver.getStatus(), driver.getRating(), driver.getRatingCount());
    }
}
