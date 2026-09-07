package com.ridehailing.pricing;

import com.ridehailing.domain.enums.CarType;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.Objects;

@Component
@EnableConfigurationProperties(PricingProperties.class)
public class CumulativeTieredPricingStrategy implements PricingStrategy {

    private static final int MONEY_SCALE = 2;
    private static final RoundingMode MONEY_ROUNDING = RoundingMode.HALF_UP;

    private final PricingProperties properties;

    public CumulativeTieredPricingStrategy(PricingProperties properties) {
        this.properties = Objects.requireNonNull(properties, "pricing properties are required");
    }

    @Override
    public BigDecimal calculateFare(CarType carType, BigDecimal distance) {
        Objects.requireNonNull(carType, "car type is required");
        Objects.requireNonNull(distance, "distance is required");
        validateDistance(distance);

        List<PricingTier> tiers = properties.getTiers().get(carType);
        if (tiers == null || tiers.isEmpty()) {
            throw new IllegalArgumentException("No pricing tiers configured for " + carType);
        }

        validateTiers(tiers);
        BigDecimal calculatedFare = calculateTieredFare(tiers, distance);
        BigDecimal minimumFare = requireNonNegative(properties.getMinimumFareFloor(), "minimum fare floor");
        return calculatedFare.max(minimumFare).setScale(MONEY_SCALE, MONEY_ROUNDING);
    }

    private BigDecimal calculateTieredFare(List<PricingTier> tiers, BigDecimal distance) {
        BigDecimal consumedDistance = BigDecimal.ZERO;
        BigDecimal fare = BigDecimal.ZERO;

        for (PricingTier tier : tiers) {
            if (consumedDistance.compareTo(distance) >= 0) {
                break;
            }
            BigDecimal rate = tier.ratePerDistance();
            BigDecimal maximum = tier.maxDistance();

            if (maximum != null) {
                BigDecimal tierDistance = distance.min(maximum).subtract(consumedDistance);
                if (tierDistance.signum() > 0) {
                    fare = fare.add(tierDistance.multiply(rate));
                    consumedDistance = consumedDistance.add(tierDistance);
                }
            } else {
                BigDecimal remainingDistance = distance.subtract(consumedDistance);
                fare = fare.add(remainingDistance.multiply(rate));
                consumedDistance = distance;
            }
        }

        if (consumedDistance.compareTo(distance) < 0) {
            throw new IllegalArgumentException("pricing tiers must include an open-ended final tier");
        }
        return fare;
    }

    private void validateTiers(List<PricingTier> tiers) {
        BigDecimal previousMaximum = BigDecimal.ZERO;
        for (int index = 0; index < tiers.size(); index++) {
            PricingTier tier = Objects.requireNonNull(tiers.get(index), "pricing tier is required");
            requireNonNegative(tier.ratePerDistance(), "tier rate");

            if (tier.maxDistance() == null) {
                if (index != tiers.size() - 1) {
                    throw new IllegalArgumentException("open-ended tier must be last");
                }
                continue;
            }

            if (tier.maxDistance().signum() <= 0) {
                throw new IllegalArgumentException("tier maximum distance must be positive");
            }
            if (tier.maxDistance().compareTo(previousMaximum) <= 0) {
                throw new IllegalArgumentException("pricing tier maximum distances must be increasing");
            }
            previousMaximum = tier.maxDistance();
        }
    }

    private void validateDistance(BigDecimal distance) {
        if (distance.signum() < 0) {
            throw new IllegalArgumentException("distance must be non-negative");
        }
    }

    private BigDecimal requireNonNegative(BigDecimal value, String fieldName) {
        if (value == null || value.signum() < 0) {
            throw new IllegalArgumentException(fieldName + " must be non-negative");
        }
        return value;
    }
}
