package com.ridehailing.domain;

import java.math.BigDecimal;
import java.util.Objects;

/**
 * Promotional coupon. {@code code} is unique and stored in uppercase.
 * Per-user usage caps are enforced by the coupon service, not by this entity.
 */
public class Coupon {

    private String code;
    private BigDecimal discountPercentage;
    private BigDecimal maxDiscountAmount;
    private Integer maxUsagePerUser;
    private boolean active = true;

    public Coupon() {
    }

    public Coupon(String code, BigDecimal discountPercentage, BigDecimal maxDiscountAmount, Integer maxUsagePerUser) {
        setCode(code);
        this.discountPercentage = discountPercentage;
        this.maxDiscountAmount = maxDiscountAmount;
        this.maxUsagePerUser = maxUsagePerUser;
        this.active = true;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code == null ? null : code.toUpperCase();
    }

    public BigDecimal getDiscountPercentage() {
        return discountPercentage;
    }

    public void setDiscountPercentage(BigDecimal discountPercentage) {
        this.discountPercentage = discountPercentage;
    }

    public BigDecimal getMaxDiscountAmount() {
        return maxDiscountAmount;
    }

    public void setMaxDiscountAmount(BigDecimal maxDiscountAmount) {
        this.maxDiscountAmount = maxDiscountAmount;
    }

    public Integer getMaxUsagePerUser() {
        return maxUsagePerUser;
    }

    public void setMaxUsagePerUser(Integer maxUsagePerUser) {
        this.maxUsagePerUser = maxUsagePerUser;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof Coupon coupon) || code == null || coupon.code == null) {
            return false;
        }
        return code.equals(coupon.code);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(code);
    }
}
