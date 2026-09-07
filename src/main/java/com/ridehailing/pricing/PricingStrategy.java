package com.ridehailing.pricing;

import com.ridehailing.domain.enums.CarType;

import java.math.BigDecimal;

public interface PricingStrategy {

    BigDecimal calculateFare(CarType carType, BigDecimal distance);

    default BigDecimal calculateFare(CarType carType, double distance) {
        return calculateFare(carType, BigDecimal.valueOf(distance));
    }
}
