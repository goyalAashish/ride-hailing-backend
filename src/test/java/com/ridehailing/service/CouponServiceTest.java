package com.ridehailing.service;

import com.ridehailing.coupon.CouponDiscount;
import com.ridehailing.dto.request.CreateCouponRequest;
import com.ridehailing.dto.response.CouponResponse;
import com.ridehailing.exception.BadRequestException;
import com.ridehailing.exception.DuplicateResourceException;
import com.ridehailing.exception.ResourceNotFoundException;
import com.ridehailing.repository.CouponRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class CouponServiceTest {

    private CouponService couponService;

    @BeforeEach
    void setUp() {
        couponService = new CouponService(new CouponRepository());
    }

    @Test
    void create_normalizesCodeAndReturnsActiveCoupon() {
        CouponResponse response = couponService.create(
                new CreateCouponRequest(" save10 ", new BigDecimal("10"), new BigDecimal("50"), 2));

        assertThat(response.code()).isEqualTo("SAVE10");
        assertThat(response.active()).isTrue();
    }

    @Test
    void create_rejectsDuplicateCodeCaseInsensitively() {
        couponService.create(new CreateCouponRequest("SAVE10", new BigDecimal("10"), new BigDecimal("50"), 1));

        assertThatThrownBy(() -> couponService.create(
                new CreateCouponRequest("save10", new BigDecimal("20"), new BigDecimal("50"), 1)))
                .isInstanceOf(DuplicateResourceException.class);
    }

    @Test
    void apply_calculatesCappedDiscountAndTracksPerUserUsage() {
        couponService.create(new CreateCouponRequest("SAVE10", new BigDecimal("10"), new BigDecimal("50"), 1));

        CouponDiscount discount = couponService.apply(7L, "save10", new BigDecimal("600"));

        assertThat(discount.couponCode()).isEqualTo("SAVE10");
        assertThat(discount.discountAmount()).isEqualByComparingTo("50.00");
        assertThatThrownBy(() -> couponService.apply(7L, "SAVE10", new BigDecimal("600")))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("usage limit");
        assertThat(couponService.apply(8L, "SAVE10", new BigDecimal("100")).discountAmount())
                .isEqualByComparingTo("10.00");
    }

    @Test
    void deactivate_preventsFutureApplication() {
        couponService.create(new CreateCouponRequest("SAVE10", new BigDecimal("10"), new BigDecimal("50"), 1));
        CouponResponse response = couponService.deactivate("save10");

        assertThat(response.active()).isFalse();
        assertThatThrownBy(() -> couponService.apply(1L, "SAVE10", new BigDecimal("100")))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("inactive");
    }

    @Test
    void apply_rejectsUnknownCoupon() {
        assertThatThrownBy(() -> couponService.apply(1L, "UNKNOWN", new BigDecimal("100")))
                .isInstanceOf(ResourceNotFoundException.class);
    }
}
