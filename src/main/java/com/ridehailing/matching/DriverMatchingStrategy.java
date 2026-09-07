package com.ridehailing.matching;

import com.ridehailing.domain.Driver;
import com.ridehailing.domain.Location;
import com.ridehailing.domain.enums.CarType;

import java.util.Collection;
import java.util.Optional;

public interface DriverMatchingStrategy {

    default String name() {
        return getClass().getSimpleName().replace("DriverStrategy", "").toUpperCase();
    }

    Optional<Driver> match(
            Location pickupLocation,
            CarType requestedCarType,
            Collection<Driver> candidateDrivers,
            double searchRadius);
}
