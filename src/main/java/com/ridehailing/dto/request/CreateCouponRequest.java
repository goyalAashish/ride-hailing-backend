package com.ridehailing.dto.request;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

public record CreateCouponRequest(
        @NotBlank(message = "must not be blank") String code,
        @NotNull(message = "must not be null")
        @DecimalMin(value = "0.01", message = "must be greater than 0")
        @DecimalMax(value = "100.00", message = "must be at most 100")
        BigDecimal discountPercentage,
        @NotNull(message = "must not be null")
        @DecimalMin(value = "0.00", message = "must be non-negative")
        BigDecimal maxDiscountAmount,
        @NotNull(message = "must not be null")
        @Min(value = 1, message = "must be at least 1")
        Integer maxUsagePerUser
) {
}
