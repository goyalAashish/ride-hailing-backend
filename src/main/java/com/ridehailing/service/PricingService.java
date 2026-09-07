package com.ridehailing.service;

import com.ridehailing.domain.enums.CarType;
import com.ridehailing.domain.Location;
import com.ridehailing.pricing.PricingStrategy;
import com.ridehailing.surge.SurgePricingService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

@Service
public class PricingService {

    private final PricingStrategy pricingStrategy;
    private final SurgePricingService surgePricingService;

    public PricingService(PricingStrategy pricingStrategy) {
        this(pricingStrategy, null);
    }

    @Autowired
    public PricingService(PricingStrategy pricingStrategy, SurgePricingService surgePricingService) {
        this.pricingStrategy = pricingStrategy;
        this.surgePricingService = surgePricingService;
    }

    public BigDecimal calculateFare(CarType carType, BigDecimal distance) {
        return pricingStrategy.calculateFare(carType, distance);
    }

    public BigDecimal calculateFare(CarType carType, BigDecimal distance, Location pickupLocation) {
        return calculateFare(carType, distance, surgeMultiplier(pickupLocation));
    }

    public BigDecimal calculateFare(CarType carType, BigDecimal distance, BigDecimal multiplier) {
        BigDecimal rawFare = pricingStrategy.calculateFareBeforeMinimum(carType, distance);
        BigDecimal adjustedFare = rawFare.multiply(multiplier)
                .setScale(2, java.math.RoundingMode.HALF_UP);
        return pricingStrategy.applyMinimumFare(adjustedFare);
    }

    public BigDecimal surgeMultiplier(Location pickupLocation) {
        return surgePricingService == null || pickupLocation == null
                ? BigDecimal.ONE
                : surgePricingService.calculateMultiplier(pickupLocation);
    }
}
