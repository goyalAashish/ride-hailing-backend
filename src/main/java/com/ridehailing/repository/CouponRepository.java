package com.ridehailing.repository;

import com.ridehailing.domain.Coupon;
import org.springframework.stereotype.Repository;

import java.util.Objects;
import java.util.Optional;

@Repository
public class CouponRepository extends InMemoryRepository<String, Coupon> {

    /**
     * Upserts by uppercase coupon code (natural key). Callers that need create-only
     * uniqueness should check {@link #existsById(String)} first.
     */
    public Coupon save(Coupon coupon) {
        Objects.requireNonNull(coupon, "coupon is required");
        Objects.requireNonNull(coupon.getCode(), "code is required");
        coupon.setCode(coupon.getCode());
        store.put(coupon.getCode(), coupon);
        return coupon;
    }

    public Optional<Coupon> findByCode(String code) {
        if (code == null) {
            return Optional.empty();
        }
        return findById(code.toUpperCase());
    }

    public boolean existsByCode(String code) {
        return code != null && existsById(code.toUpperCase());
    }

    public boolean deleteByCode(String code) {
        if (code == null) {
            return false;
        }
        return deleteById(code.toUpperCase());
    }
}
