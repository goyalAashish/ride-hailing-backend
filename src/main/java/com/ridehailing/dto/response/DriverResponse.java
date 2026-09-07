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
        DriverStatus status
) {

    public static DriverResponse from(Driver driver) {
        return new DriverResponse(
                driver.getDriverId(),
                driver.getName(),
                driver.getPhone(),
                driver.getRegistrationNumber(),
                driver.getCarType(),
                driver.getStatus());
    }
}
