package com.ridehailing.repository;

import com.ridehailing.domain.Coupon;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;

class CouponRepositoryTest {

    private CouponRepository repository;

    @BeforeEach
    void setUp() {
        repository = new CouponRepository();
    }

    @Test
    void save_normalizesCodeToUpperCase() {
        Coupon coupon = repository.save(coupon("save10", "10", "50", 1));

        assertThat(coupon.getCode()).isEqualTo("SAVE10");
        assertThat(repository.findByCode("save10")).contains(coupon);
        assertThat(repository.existsByCode("Save10")).isTrue();
    }

    @Test
    void save_sameCode_overwritesForUpdate() {
        repository.save(coupon("SAVE10", "10", "50", 1));
        Coupon updated = coupon("save10", "20", "80", 2);
        updated.setActive(false);
        repository.save(updated);

        Coupon stored = repository.findByCode("SAVE10").orElseThrow();
        assertThat(stored.getDiscountPercentage()).isEqualByComparingTo("20");
        assertThat(stored.getMaxUsagePerUser()).isEqualTo(2);
        assertThat(stored.isActive()).isFalse();
        assertThat(repository.count()).isEqualTo(1);
    }

    @Test
    void deleteByCode_isCaseInsensitive() {
        repository.save(coupon("SAVE10", "10", "50", 1));

        assertThat(repository.deleteByCode("save10")).isTrue();
        assertThat(repository.findByCode("SAVE10")).isEmpty();
    }

    private static Coupon coupon(String code, String percent, String cap, int maxUsage) {
        return new Coupon(code, new BigDecimal(percent), new BigDecimal(cap), maxUsage);
    }
}
