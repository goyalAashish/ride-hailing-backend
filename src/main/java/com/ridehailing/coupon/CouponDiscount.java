package com.ridehailing.coupon;

import java.math.BigDecimal;

public record CouponDiscount(String couponCode, BigDecimal discountAmount) {
}
