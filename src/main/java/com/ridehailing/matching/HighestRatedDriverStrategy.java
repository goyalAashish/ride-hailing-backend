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
public class HighestRatedDriverStrategy implements DriverMatchingStrategy {

    private final DistanceCalculator distanceCalculator;

    public HighestRatedDriverStrategy(DistanceCalculator distanceCalculator) {
        this.distanceCalculator = distanceCalculator;
    }

    @Override
    public String name() {
        return "HIGHEST_RATED";
    }

    @Override
    public Optional<Driver> match(Location pickupLocation, CarType requestedCarType,
                                   Collection<Driver> candidateDrivers, double searchRadius) {
        Objects.requireNonNull(pickupLocation, "pickup location is required");
        Objects.requireNonNull(requestedCarType, "requested car type is required");
        if (!Double.isFinite(searchRadius) || searchRadius < 0) {
            throw new IllegalArgumentException("search radius must be a finite non-negative value");
        }
        Optional<Driver> result = find(pickupLocation, requestedCarType, candidateDrivers, searchRadius);
        if (result.isPresent() || requestedCarType != CarType.HATCHBACK) {
            return result;
        }
        return find(pickupLocation, CarType.SEDAN, candidateDrivers, searchRadius);
    }

    private Optional<Driver> find(Location pickup, CarType type, Collection<Driver> drivers, double radius) {
        return drivers.stream()
                .filter(d -> d != null && d.getStatus() == DriverStatus.AVAILABLE
                        && d.getCarType() == type && d.getCurrentLocation() != null)
                .map(d -> new RatedDriver(d, distanceCalculator.between(pickup, d.getCurrentLocation())))
                .filter(d -> d.distance <= radius)
                .min(Comparator.comparingDouble((RatedDriver d) -> d.driver.getRating()).reversed()
                        .thenComparingDouble(d -> d.distance)
                        .thenComparing(d -> d.driver.getDriverId(),
                                Comparator.nullsLast(Comparator.naturalOrder())))
                .map(d -> d.driver);
    }

    private record RatedDriver(Driver driver, double distance) {
    }
}
