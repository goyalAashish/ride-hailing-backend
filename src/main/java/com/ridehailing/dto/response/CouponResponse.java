package com.ridehailing.dto.response;

import com.ridehailing.domain.Coupon;

import java.math.BigDecimal;

public record CouponResponse(
        String code,
        BigDecimal discountPercentage,
        BigDecimal maxDiscountAmount,
        Integer maxUsagePerUser,
        boolean active
) {

    public static CouponResponse from(Coupon coupon) {
        return new CouponResponse(
                coupon.getCode(),
                coupon.getDiscountPercentage(),
                coupon.getMaxDiscountAmount(),
                coupon.getMaxUsagePerUser(),
                coupon.isActive());
    }
}
