package com.ridehailing.service;

import com.ridehailing.coupon.CouponDiscount;
import com.ridehailing.domain.Coupon;
import com.ridehailing.dto.request.CreateCouponRequest;
import com.ridehailing.dto.response.CouponResponse;
import com.ridehailing.exception.BadRequestException;
import com.ridehailing.exception.DuplicateResourceException;
import com.ridehailing.exception.ResourceNotFoundException;
import com.ridehailing.repository.CouponRepository;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.atomic.AtomicInteger;

@Service
public class CouponService {

    private static final int MONEY_SCALE = 2;
    private static final RoundingMode MONEY_ROUNDING = RoundingMode.HALF_UP;

    private final CouponRepository couponRepository;
    private final ConcurrentMap<Long, ConcurrentMap<String, AtomicInteger>> usageByUser = new ConcurrentHashMap<>();

    public CouponService(CouponRepository couponRepository) {
        this.couponRepository = couponRepository;
    }

    public CouponResponse create(CreateCouponRequest request) {
        String code = request.code().trim().toUpperCase();
        if (couponRepository.existsByCode(code)) {
            throw new DuplicateResourceException("Coupon", "code", code);
        }

        Coupon coupon = new Coupon(
                code,
                request.discountPercentage(),
                request.maxDiscountAmount(),
                request.maxUsagePerUser());
        return CouponResponse.from(couponRepository.save(coupon));
    }

    public CouponResponse deactivate(String code) {
        Coupon coupon = getRequired(code);
        coupon.setActive(false);
        return CouponResponse.from(couponRepository.save(coupon));
    }

    public CouponDiscount apply(Long userId, String code, BigDecimal baseFare) {
        if (userId == null) {
            throw new BadRequestException("INVALID_USER", "User id is required to apply a coupon");
        }
        if (baseFare == null || baseFare.signum() < 0) {
            throw new BadRequestException("INVALID_FARE", "Base fare must be non-negative");
        }

        Coupon coupon = getRequired(code);
        if (!coupon.isActive()) {
            throw new BadRequestException("COUPON_INACTIVE", "Coupon is inactive: " + coupon.getCode());
        }

        String normalizedCode = coupon.getCode();
        AtomicInteger usage = usageByUser
                .computeIfAbsent(userId, ignored -> new ConcurrentHashMap<>())
                .computeIfAbsent(normalizedCode, ignored -> new AtomicInteger());

        synchronized (usage) {
            if (usage.get() >= coupon.getMaxUsagePerUser()) {
                throw new BadRequestException(
                        "COUPON_USAGE_LIMIT_EXCEEDED",
                        "Coupon usage limit exceeded for user: " + normalizedCode);
            }

            usage.incrementAndGet();
            BigDecimal discount = baseFare
                    .multiply(coupon.getDiscountPercentage())
                    .divide(new BigDecimal("100"), 10, MONEY_ROUNDING)
                    .min(coupon.getMaxDiscountAmount())
                    .min(baseFare)
                    .setScale(MONEY_SCALE, MONEY_ROUNDING);
            return new CouponDiscount(normalizedCode, discount);
        }
    }

    private Coupon getRequired(String code) {
        return couponRepository.findByCode(code)
                .orElseThrow(() -> new ResourceNotFoundException("Coupon", code));
    }
}
