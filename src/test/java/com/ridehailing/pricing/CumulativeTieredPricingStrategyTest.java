package com.ridehailing.pricing;

import com.ridehailing.domain.enums.CarType;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.EnumMap;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class CumulativeTieredPricingStrategyTest {

    @Test
    void calculateFare_appliesCumulativeDistanceTiers() {
        CumulativeTieredPricingStrategy strategy = strategy(
                new BigDecimal("50.00"),
                List.of(
                        new PricingTier(new BigDecimal("5"), new BigDecimal("10")),
                        new PricingTier(new BigDecimal("10"), new BigDecimal("8")),
                        new PricingTier(null, new BigDecimal("6"))));

        assertThat(strategy.calculateFare(CarType.SEDAN, new BigDecimal("12")))
                .isEqualByComparingTo("102.00");
    }

    @Test
    void calculateFare_usesEachTierBoundaryCorrectly() {
        CumulativeTieredPricingStrategy strategy = strategy(
                BigDecimal.ZERO,
                List.of(
                        new PricingTier(new BigDecimal("5"), new BigDecimal("10")),
                        new PricingTier(null, new BigDecimal("8"))));

        assertThat(strategy.calculateFare(CarType.SEDAN, new BigDecimal("5")))
                .isEqualByComparingTo("50.00");
        assertThat(strategy.calculateFare(CarType.SEDAN, new BigDecimal("6")))
                .isEqualByComparingTo("58.00");
    }

    @Test
    void calculateFare_appliesMinimumFareFloorBeforeScaling() {
        CumulativeTieredPricingStrategy strategy = strategy(
                new BigDecimal("50"),
                List.of(new PricingTier(null, new BigDecimal("1.234"))));

        BigDecimal fare = strategy.calculateFare(CarType.SEDAN, new BigDecimal("1"));

        assertThat(fare).isEqualByComparingTo("50.00");
        assertThat(fare.scale()).isEqualTo(2);
    }

    @Test
    void calculateFare_roundsHalfUpToTwoDecimals() {
        CumulativeTieredPricingStrategy strategy = strategy(
                BigDecimal.ZERO,
                List.of(new PricingTier(null, new BigDecimal("1"))));

        assertThat(strategy.calculateFare(CarType.SEDAN, new BigDecimal("1.235")))
                .isEqualByComparingTo("1.24");
    }

    @Test
    void calculateFare_rejectsMissingOpenEndedTier() {
        CumulativeTieredPricingStrategy strategy = strategy(
                BigDecimal.ZERO,
                List.of(new PricingTier(new BigDecimal("5"), new BigDecimal("10"))));

        assertThatThrownBy(() -> strategy.calculateFare(CarType.SEDAN, new BigDecimal("6")))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("open-ended");
    }

    @Test
    void calculateFare_rejectsNegativeDistance() {
        CumulativeTieredPricingStrategy strategy = strategy(
                BigDecimal.ZERO,
                List.of(new PricingTier(null, new BigDecimal("10"))));

        assertThatThrownBy(() -> strategy.calculateFare(CarType.SEDAN, new BigDecimal("-1")))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void calculateFare_rejectsInvalidTierOrdering() {
        CumulativeTieredPricingStrategy strategy = strategy(
                BigDecimal.ZERO,
                List.of(
                        new PricingTier(new BigDecimal("10"), new BigDecimal("10")),
                        new PricingTier(new BigDecimal("5"), new BigDecimal("8")),
                        new PricingTier(null, new BigDecimal("6"))));

        assertThatThrownBy(() -> strategy.calculateFare(CarType.SEDAN, new BigDecimal("1")))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("increasing");
    }

    private static CumulativeTieredPricingStrategy strategy(
            BigDecimal minimumFare,
            List<PricingTier> tiers) {
        PricingProperties properties = new PricingProperties();
        properties.setMinimumFareFloor(minimumFare);
        EnumMap<CarType, List<PricingTier>> configuredTiers = new EnumMap<>(CarType.class);
        configuredTiers.put(CarType.SEDAN, tiers);
        properties.setTiers(configuredTiers);
        return new CumulativeTieredPricingStrategy(properties);
    }
}
