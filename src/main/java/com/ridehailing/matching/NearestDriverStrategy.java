package com.ridehailing.matching;

import com.ridehailing.domain.Driver;
import com.ridehailing.domain.Location;
import com.ridehailing.domain.enums.CarType;
import com.ridehailing.domain.enums.DriverStatus;
import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.Comparator;
import java.util.Objects;
import java.util.Optional;

@Component
public class NearestDriverStrategy implements DriverMatchingStrategy {

    private final DistanceCalculator distanceCalculator;

    public NearestDriverStrategy(DistanceCalculator distanceCalculator) {
        this.distanceCalculator = distanceCalculator;
    }

    @Override
    public Optional<Driver> match(
            Location pickupLocation,
            CarType requestedCarType,
            Collection<Driver> candidateDrivers,
            double searchRadius) {
        Objects.requireNonNull(pickupLocation, "pickup location is required");
        Objects.requireNonNull(requestedCarType, "requested car type is required");
        Objects.requireNonNull(candidateDrivers, "candidate drivers are required");
        validateSearchRadius(searchRadius);

        Optional<Driver> exactMatch = findNearest(
                pickupLocation, requestedCarType, candidateDrivers, searchRadius);
        if (exactMatch.isPresent() || requestedCarType != CarType.HATCHBACK) {
            return exactMatch;
        }
        return findNearest(pickupLocation, CarType.SEDAN, candidateDrivers, searchRadius);
    }

    private Optional<Driver> findNearest(
            Location pickupLocation,
            CarType requestedCarType,
            Collection<Driver> candidateDrivers,
            double searchRadius) {
        return candidateDrivers.stream()
                .filter(driver -> isEligible(driver, requestedCarType))
                .map(driver -> new DriverDistance(
                        driver,
                        distanceCalculator.between(pickupLocation, driver.getCurrentLocation())))
                .filter(driverDistance -> driverDistance.distance() <= searchRadius)
                .min(Comparator
                        .comparingDouble(DriverDistance::distance)
                        .thenComparing(driverDistance -> driverDistance.driver().getDriverId(),
                                Comparator.nullsLast(Comparator.naturalOrder())))
                .map(DriverDistance::driver);
    }

    private boolean isEligible(Driver driver, CarType requestedCarType) {
        return driver != null
                && driver.getStatus() == DriverStatus.AVAILABLE
                && driver.getCarType() == requestedCarType
                && driver.getCurrentLocation() != null;
    }

    private void validateSearchRadius(double searchRadius) {
        if (!Double.isFinite(searchRadius) || searchRadius < 0) {
            throw new IllegalArgumentException("search radius must be a finite non-negative value");
        }
    }

    private record DriverDistance(Driver driver, double distance) {
    }
}
