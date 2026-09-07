package com.ridehailing.surge;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;

@Component
public class DemandSupplySurgePricingStrategy implements SurgePricingStrategy {

    private final BigDecimal maximumMultiplier;

    public DemandSupplySurgePricingStrategy(
            @Value("${ride-hailing.surge.maximum-multiplier:3.0}") BigDecimal maximumMultiplier) {
        if (maximumMultiplier.signum() < 0) {
            throw new IllegalArgumentException("maximum multiplier must be non-negative");
        }
        this.maximumMultiplier = maximumMultiplier;
    }

    public DemandSupplySurgePricingStrategy() {
        this(new BigDecimal("3.0"));
    }

    public DemandSupplySurgePricingStrategy(double maximumMultiplier) {
        this(BigDecimal.valueOf(maximumMultiplier));
    }

    @Override
    public BigDecimal multiplier(double demand, double supply) {
        if (!Double.isFinite(demand) || !Double.isFinite(supply) || demand < 0 || supply < 0) {
            throw new IllegalArgumentException("demand and supply must be finite and non-negative");
        }
        BigDecimal ratio = BigDecimal.valueOf(demand)
                .divide(BigDecimal.valueOf(supply + 1), 10, RoundingMode.HALF_UP);
        BigDecimal multiplier;
        if (ratio.compareTo(BigDecimal.ONE) < 0) {
            multiplier = BigDecimal.ONE;
        } else if (ratio.compareTo(new BigDecimal("2")) < 0) {
            multiplier = new BigDecimal("1.25");
        } else if (ratio.compareTo(new BigDecimal("3")) < 0) {
            multiplier = new BigDecimal("1.50");
        } else {
            multiplier = new BigDecimal("2.00");
        }
        return multiplier.min(maximumMultiplier).setScale(2, RoundingMode.HALF_UP);
    }
}
