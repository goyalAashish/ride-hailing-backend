package com.ridehailing.surge;

import com.ridehailing.domain.Location;
import com.ridehailing.domain.enums.CarType;
import com.ridehailing.service.PricingService;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;

class SurgePricingTest {

    @Test
    void demandSupplyRatioDrivesCappedMultiplier() {
        DemandSupplySurgePricingStrategy strategy = new DemandSupplySurgePricingStrategy(3.0);
        assertThat(strategy.calculateMultiplier(1, 2)).isEqualByComparingTo("1.00");
        assertThat(strategy.calculateMultiplier(2, 1)).isEqualByComparingTo("1.25");
        assertThat(strategy.calculateMultiplier(5, 1)).isEqualByComparingTo("1.50");
        assertThat(strategy.calculateMultiplier(10, 1)).isEqualByComparingTo("2.00");
    }

    @Test
    void areaValuesCanBeUpdatedConcurrentlyAndAppliedToFare() {
        SurgePricingService service = new SurgePricingService(new DemandSupplySurgePricingStrategy());
        service.setDemandSupply("downtown", 3, 1);
        assertThat(service.getDemandSupplyRatio("downtown")).isEqualByComparingTo("1.5");
        assertThat(service.calculateMultiplier("downtown")).isEqualByComparingTo("1.25");
        assertThat(service.calculateMultiplier("missing")).isEqualByComparingTo(BigDecimal.ONE);
    }

    @Test
    void pricingServiceAppliesAreaMultiplierToFare() {
        SurgePricingService service = new SurgePricingService(new DemandSupplySurgePricingStrategy());
        service.setDemandSupply("0:0", 2, 0);
        PricingService pricing = new PricingService((type, distance) -> new BigDecimal("50.00"), service);
        assertThat(pricing.calculateFare(CarType.SEDAN, BigDecimal.ONE, new Location(0.2, 0.3)))
                .isEqualByComparingTo("75.00");
    }
}
