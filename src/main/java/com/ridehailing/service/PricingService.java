package com.ridehailing.service;

import com.ridehailing.domain.enums.CarType;
import com.ridehailing.pricing.PricingStrategy;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

@Service
public class PricingService {

    private final PricingStrategy pricingStrategy;

    public PricingService(PricingStrategy pricingStrategy) {
        this.pricingStrategy = pricingStrategy;
    }

    public BigDecimal calculateFare(CarType carType, BigDecimal distance) {
        return pricingStrategy.calculateFare(carType, distance);
    }
}
