package com.ridehailing.surge;

import java.math.BigDecimal;

@FunctionalInterface
public interface SurgePricingStrategy {

    BigDecimal multiplier(double demand, double supply);

    default BigDecimal calculateMultiplier(double demand, double supply) {
        return multiplier(demand, supply);
    }

    default BigDecimal calculateSurgeMultiplier(double demand, double supply) {
        return multiplier(demand, supply);
    }

    default BigDecimal multiplier(String area, double demand, double supply) {
        return multiplier(demand, supply);
    }

    default BigDecimal calculateMultiplier(String area, double demand, double supply) {
        return multiplier(area, demand, supply);
    }
}
