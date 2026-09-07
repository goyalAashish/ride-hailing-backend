package com.ridehailing.surge;

import com.ridehailing.domain.Driver;
import com.ridehailing.domain.Location;
import com.ridehailing.domain.Ride;
import com.ridehailing.domain.enums.DriverStatus;
import com.ridehailing.domain.enums.RideStatus;
import com.ridehailing.repository.DriverRepository;
import com.ridehailing.repository.RideRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Clock;
import java.time.LocalDateTime;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

@Service
public class SurgePricingService {

    private final SurgePricingStrategy strategy;
    private final ConcurrentMap<String, DemandSupply> configuredAreas = new ConcurrentHashMap<>();
    private final RideRepository rideRepository;
    private final DriverRepository driverRepository;
    private final Clock clock;

    public SurgePricingService(SurgePricingStrategy strategy) {
        this(strategy, null, null, Clock.systemDefaultZone());
    }

    @Autowired
    public SurgePricingService(
            SurgePricingStrategy strategy,
            RideRepository rideRepository,
            DriverRepository driverRepository) {
        this(strategy, rideRepository, driverRepository, Clock.systemDefaultZone());
    }

    public SurgePricingService(
            SurgePricingStrategy strategy,
            RideRepository rideRepository,
            DriverRepository driverRepository,
            Clock clock) {
        this.strategy = strategy;
        this.rideRepository = rideRepository;
        this.driverRepository = driverRepository;
        this.clock = clock;
    }

    public void setDemandSupply(String area, int demand, int supply) {
        validate(area, demand, supply);
        configuredAreas.put(area.trim(), new DemandSupply(demand, supply));
    }

    public void updateArea(String area, int demand, int supply) {
        setDemandSupply(area, demand, supply);
    }

    public void setDemandSupply(Location location, int demand, int supply) {
        setDemandSupply(areaFor(location), demand, supply);
    }

    public BigDecimal getDemandSupplyRatio(String area) {
        DemandSupply value = configuredAreas.getOrDefault(area, new DemandSupply(0, 0));
        return ratio(value);
    }

    public BigDecimal getDemandSupplyRatio(Location location) {
        return ratio(currentDemandSupply(location));
    }

    public BigDecimal calculateMultiplier(String area) {
        DemandSupply value = configuredAreas.getOrDefault(area, new DemandSupply(0, 0));
        return strategy.multiplier(value.demand(), value.supply());
    }

    public BigDecimal calculateSurgeMultiplier(String area) {
        return calculateMultiplier(area);
    }

    public BigDecimal calculateSurgeMultiplier(Location location) {
        return calculateMultiplier(location);
    }

    public BigDecimal calculateMultiplier(Location location) {
        DemandSupply value = currentDemandSupply(location);
        return strategy.multiplier(value.demand(), value.supply());
    }

    public String areaFor(Location location) {
        if (location == null) {
            return "unknown";
        }
        return "%d:%d".formatted(
                (long) Math.floor(location.x() / 10.0),
                (long) Math.floor(location.y() / 10.0));
    }

    public void recordDemand(String area) {
        configuredAreas.compute(area, (key, value) -> value == null
                ? new DemandSupply(1, 0)
                : new DemandSupply(value.demand() + 1, value.supply()));
    }

    public void recordSupply(String area) {
        configuredAreas.compute(area, (key, value) -> value == null
                ? new DemandSupply(0, 1)
                : new DemandSupply(value.demand(), value.supply() + 1));
    }

    private DemandSupply currentDemandSupply(Location location) {
        if (rideRepository == null || driverRepository == null) {
            return configuredAreas.getOrDefault(areaFor(location), new DemandSupply(0, 0));
        }

        LocalDateTime cutoff = LocalDateTime.now(clock).minusMinutes(5);
        int demand = (int) rideRepository.findByStatus(RideStatus.REQUESTED).stream()
                .filter(ride -> ride.getCreatedAt() != null && !ride.getCreatedAt().isBefore(cutoff))
                .filter(ride -> sameArea(ride, location))
                .count();
        int supply = (int) driverRepository.findByStatus(DriverStatus.AVAILABLE).stream()
                .filter(driver -> sameArea(driver, location))
                .count();
        return new DemandSupply(demand, supply);
    }

    private boolean sameArea(Ride ride, Location location) {
        return ride.getPickupLocation() != null
                && areaFor(ride.getPickupLocation()).equals(areaFor(location));
    }

    private boolean sameArea(Driver driver, Location location) {
        return driver.getCurrentLocation() != null
                && areaFor(driver.getCurrentLocation()).equals(areaFor(location));
    }

    private BigDecimal ratio(DemandSupply value) {
        return BigDecimal.valueOf(value.demand())
                .divide(BigDecimal.valueOf(value.supply() + 1), 10, RoundingMode.HALF_UP);
    }

    private void validate(String area, int demand, int supply) {
        if (area == null || area.isBlank() || demand < 0 || supply < 0) {
            throw new IllegalArgumentException("area, demand and supply must be valid");
        }
    }

    public record DemandSupply(int demand, int supply) {
    }
}
