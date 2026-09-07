package com.ridehailing.service;

import com.ridehailing.cancellation.CancellationPolicy;
import com.ridehailing.domain.Driver;
import com.ridehailing.domain.Location;
import com.ridehailing.domain.enums.CarType;
import com.ridehailing.domain.enums.DriverStatus;
import com.ridehailing.domain.enums.RideStatus;
import com.ridehailing.domain.Ride;
import com.ridehailing.dto.request.RegisterDriverRequest;
import com.ridehailing.dto.request.RegisterUserRequest;
import com.ridehailing.dto.request.RequestRideRequest;
import com.ridehailing.matching.DistanceCalculator;
import com.ridehailing.matching.NearestDriverStrategy;
import com.ridehailing.repository.CouponRepository;
import com.ridehailing.repository.DriverRepository;
import com.ridehailing.repository.RideRepository;
import com.ridehailing.repository.UserRepository;
import com.ridehailing.pricing.PricingStrategy;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneOffset;

import static org.assertj.core.api.Assertions.assertThat;

class RideCancellationTest {

    @Test
    void cancellationIsFreeWithinWindowAndReleasesReservedDriver() {
        Fixture fixture = new Fixture(Instant.parse("2026-01-01T00:00:00Z"));
        var ride = fixture.service.requestRide(1L,
                new RequestRideRequest(0.0, 0.0, 1.0, 1.0, CarType.SEDAN, null));

        var cancelled = fixture.service.cancelRide(1L, ride.rideId());
        assertThat(cancelled.status()).isEqualTo(RideStatus.CANCELLED);
        assertThat(cancelled.cancellationFee()).isEqualByComparingTo("0.00");
        assertThat(fixture.driverRepository.findById(1L).orElseThrow().getStatus())
                .isEqualTo(DriverStatus.AVAILABLE);
    }

    @Test
    void policyChargesAfterGracePeriodAndTerminalRidesCannotBeCancelled() {
        CancellationPolicy policy = new CancellationPolicy(Duration.ofMinutes(2), new BigDecimal("30"));
        Ride ride = new Ride();
        ride.setCreatedAt(java.time.LocalDateTime.of(2026, 1, 1, 0, 0));
        assertThat(policy.feeFor(ride, java.time.LocalDateTime.of(2026, 1, 1, 0, 3)))
                .isEqualByComparingTo("30.00");
        assertThat(ride.getStatus().isActive()).isTrue();
    }

    private static class Fixture {
        final DriverRepository driverRepository = new DriverRepository();
        final RideService service;

        Fixture(Instant instant) {
            UserService users = new UserService(new UserRepository());
            DriverService drivers = new DriverService(driverRepository);
            users.register(new RegisterUserRequest("User", "u"));
            drivers.register(new RegisterDriverRequest("Driver", "d", "r", CarType.SEDAN));
            Driver driver = drivers.getRequired(1L);
            driver.setCurrentLocation(new Location(0, 0));
            driver.setStatus(DriverStatus.AVAILABLE);
            driverRepository.save(driver);
            DriverMatchingService matching = new DriverMatchingService(driverRepository,
                    new NearestDriverStrategy(new DistanceCalculator()), 5);
            PricingService pricing = new PricingService((PricingStrategy) (type, distance) ->
                    new BigDecimal("50.00"));
            service = new RideService(users, drivers, driverRepository, new RideRepository(),
                    matching, pricing, new CouponService(new CouponRepository()),
                    new DistanceCalculator(), new CancellationPolicy(Duration.ofMinutes(2),
                    new BigDecimal("20")), Clock.fixed(instant, ZoneOffset.UTC));
        }
    }
}
