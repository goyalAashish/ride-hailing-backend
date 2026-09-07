package com.ridehailing.pricing;

import java.math.BigDecimal;

public record PricingTier(BigDecimal maxDistance, BigDecimal ratePerDistance) {
}
