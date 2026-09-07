package com.ridehailing.service;

import com.ridehailing.domain.enums.CarType;
import com.ridehailing.pricing.PricingStrategy;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;

class PricingServiceTest {

    @Test
    void calculateFare_delegatesToConfiguredStrategy() {
        PricingStrategy strategy = (carType, distance) -> new BigDecimal("75.00");
        PricingService pricingService = new PricingService(strategy);

        assertThat(pricingService.calculateFare(CarType.SEDAN, new BigDecimal("10")))
                .isEqualByComparingTo("75.00");
    }
}
