package com.ridehailing.cancellation;

import com.ridehailing.domain.Ride;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Duration;
import java.time.LocalDateTime;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class CancellationPolicy {

    private final Duration freeWindow;
    private final BigDecimal fee;

    public CancellationPolicy(Duration freeWindow, BigDecimal fee) {
        if (freeWindow == null || freeWindow.isNegative() || fee == null || fee.signum() < 0) {
            throw new IllegalArgumentException("invalid cancellation policy");
        }
        this.freeWindow = freeWindow;
        this.fee = fee.setScale(2, RoundingMode.HALF_UP);
    }

    @Autowired
    public CancellationPolicy(
            @Value("${ride-hailing.cancellation.free-window-seconds:120}") long freeWindowSeconds,
            @Value("${ride-hailing.cancellation.fee:30.00}") BigDecimal fee) {
        this(Duration.ofSeconds(freeWindowSeconds), fee);
    }

    public BigDecimal feeFor(Ride ride, LocalDateTime now) {
        if (ride.getCreatedAt() == null || now == null
                || !now.isAfter(ride.getCreatedAt().plus(freeWindow))) {
            return BigDecimal.ZERO.setScale(2);
        }
        return fee;
    }

    public BigDecimal calculateCancellationFee(Ride ride, LocalDateTime now) {
        return feeFor(ride, now);
    }

    public Duration freeWindow() {
        return freeWindow;
    }
}
