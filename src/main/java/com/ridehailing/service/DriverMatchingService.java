package com.ridehailing.service;

import com.ridehailing.domain.Driver;
import com.ridehailing.domain.Location;
import com.ridehailing.domain.enums.CarType;
import com.ridehailing.domain.enums.DriverStatus;
import com.ridehailing.matching.DriverMatchingStrategy;
import com.ridehailing.repository.DriverRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class DriverMatchingService {

    private final DriverRepository driverRepository;
    private final DriverMatchingStrategy matchingStrategy;
    private final double searchRadius;

    public DriverMatchingService(
            DriverRepository driverRepository,
            DriverMatchingStrategy matchingStrategy,
            @Value("${ride-hailing.matching.search-radius-km:5.0}") double searchRadius) {
        this.driverRepository = driverRepository;
        this.matchingStrategy = matchingStrategy;
        this.searchRadius = searchRadius;
    }

    public Optional<Driver> findMatch(Location pickupLocation, CarType requestedCarType) {
        return matchingStrategy.match(
                pickupLocation,
                requestedCarType,
                driverRepository.findByStatus(DriverStatus.AVAILABLE),
                searchRadius);
    }
}
