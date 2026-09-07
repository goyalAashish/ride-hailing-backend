package com.ridehailing.service;

import com.ridehailing.domain.Driver;
import com.ridehailing.domain.Location;
import com.ridehailing.domain.enums.CarType;
import com.ridehailing.domain.enums.DriverStatus;
import com.ridehailing.matching.DriverMatchingStrategy;
import com.ridehailing.repository.DriverRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.ridehailing.exception.BadRequestException;

import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
public class DriverMatchingService {

    private final DriverRepository driverRepository;
    private final DriverMatchingStrategy matchingStrategy;
    private final Map<String, DriverMatchingStrategy> strategies;
    private final double searchRadius;

    @Autowired
    public DriverMatchingService(DriverRepository driverRepository,
                                  List<DriverMatchingStrategy> strategies,
                                  @Value("${ride-hailing.matching.search-radius-km:5.0}") double searchRadius) {
        this.driverRepository = driverRepository;
        this.strategies = strategies.stream().collect(Collectors.toUnmodifiableMap(
                strategy -> strategy.name().toUpperCase(Locale.ROOT), Function.identity(),
                (first, ignored) -> first));
        this.matchingStrategy = this.strategies.getOrDefault("NEAREST",
                this.strategies.values().stream().findFirst().orElseThrow());
        this.selectedStrategy = new java.util.concurrent.atomic.AtomicReference<>(this.matchingStrategy);
        this.searchRadius = searchRadius;
    }

    public DriverMatchingService(
            DriverRepository driverRepository,
            DriverMatchingStrategy matchingStrategy,
            @Value("${ride-hailing.matching.search-radius-km:5.0}") double searchRadius) {
        this.driverRepository = driverRepository;
        this.matchingStrategy = matchingStrategy;
        this.strategies = Map.of(matchingStrategy.name().toUpperCase(Locale.ROOT), matchingStrategy);
        this.selectedStrategy = new java.util.concurrent.atomic.AtomicReference<>(matchingStrategy);
        this.searchRadius = searchRadius;
    }

    public DriverMatchingService(DriverRepository driverRepository,
                                  DriverMatchingStrategy nearest,
                                  DriverMatchingStrategy highestRated,
                                  double searchRadius) {
        this.driverRepository = driverRepository;
        this.strategies = Map.of(nearest.name().toUpperCase(Locale.ROOT), nearest,
                highestRated.name().toUpperCase(Locale.ROOT), highestRated);
        this.matchingStrategy = nearest;
        this.selectedStrategy = new java.util.concurrent.atomic.AtomicReference<>(nearest);
        this.searchRadius = searchRadius;
    }

    private final java.util.concurrent.atomic.AtomicReference<DriverMatchingStrategy> selectedStrategy;

    public Optional<Driver> findMatch(Location pickupLocation, CarType requestedCarType) {
        return selectedStrategy.get().match(
                pickupLocation,
                requestedCarType,
                driverRepository.findByStatus(DriverStatus.AVAILABLE),
                searchRadius);
    }

    public String getStrategyName() {
        return selectedStrategy.get().name();
    }

    public String getCurrentStrategy() {
        return getStrategyName();
    }

    public DriverMatchingStrategy getMatchingStrategy() {
        return selectedStrategy.get();
    }

    public void setMatchingStrategy(String strategyName) {
        setStrategy(strategyName);
    }

    public void setMatchingStrategy(DriverMatchingStrategy strategy) {
        if (strategy == null) {
            throw new BadRequestException("INVALID_MATCHING_STRATEGY", "Matching strategy is required");
        }
        DriverMatchingStrategy registered = strategies.get(strategy.name().toUpperCase(Locale.ROOT));
        if (registered == null) {
            throw new BadRequestException("INVALID_MATCHING_STRATEGY",
                    "Matching strategy is not registered: " + strategy.name());
        }
        selectedStrategy.set(registered);
    }

    public void setStrategy(DriverMatchingStrategy strategy) {
        setMatchingStrategy(strategy);
    }

    public void setStrategy(String strategyName) {
        if (strategyName == null || strategyName.isBlank()) {
            throw new BadRequestException("INVALID_MATCHING_STRATEGY", "Matching strategy is required");
        }
        DriverMatchingStrategy strategy = strategies.get(strategyName.trim().toUpperCase(Locale.ROOT));
        if (strategy == null) {
            throw new BadRequestException("INVALID_MATCHING_STRATEGY",
                    "Unknown matching strategy: " + strategyName);
        }
        selectedStrategy.set(strategy);
    }

    /**
     * Matches and reserves a driver using a lock on that driver only. A concurrent request
     * can therefore reserve another driver without waiting on a global fleet lock.
     */
    public Optional<Driver> reserveMatch(Location pickupLocation, CarType requestedCarType) {
        int attempts = Math.max(1, driverRepository.findByStatus(DriverStatus.AVAILABLE).size());
        for (int attempt = 0; attempt < attempts; attempt++) {
            Optional<Driver> candidate = findMatch(pickupLocation, requestedCarType);
            if (candidate.isEmpty()) {
                return Optional.empty();
            }
            Driver driver = candidate.get();
            synchronized (driver) {
                if (driver.getStatus() == DriverStatus.AVAILABLE) {
                    driver.setStatus(DriverStatus.RESERVED);
                    driverRepository.save(driver);
                    return Optional.of(driver);
                }
            }
        }
        return Optional.empty();
    }
}
