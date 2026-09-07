package com.ridehailing.pricing;

import com.ridehailing.domain.enums.CarType;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.math.BigDecimal;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

@ConfigurationProperties(prefix = "ride-hailing.pricing")
public class PricingProperties {

    private BigDecimal minimumFareFloor = new BigDecimal("50.00");
    private Map<CarType, List<PricingTier>> tiers = new EnumMap<>(CarType.class);

    public BigDecimal getMinimumFareFloor() {
        return minimumFareFloor;
    }

    public void setMinimumFareFloor(BigDecimal minimumFareFloor) {
        this.minimumFareFloor = minimumFareFloor;
    }

    public Map<CarType, List<PricingTier>> getTiers() {
        return tiers;
    }

    public void setTiers(Map<CarType, List<PricingTier>> tiers) {
        this.tiers = tiers;
    }
}
