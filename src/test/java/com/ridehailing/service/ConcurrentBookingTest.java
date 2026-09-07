package com.ridehailing.service;

import com.ridehailing.domain.Driver;
import com.ridehailing.domain.Location;
import com.ridehailing.domain.enums.CarType;
import com.ridehailing.domain.enums.DriverStatus;
import com.ridehailing.dto.request.RegisterDriverRequest;
import com.ridehailing.dto.request.RegisterUserRequest;
import com.ridehailing.dto.request.RequestRideRequest;
import com.ridehailing.matching.DistanceCalculator;
import com.ridehailing.matching.NearestDriverStrategy;
import com.ridehailing.repository.CouponRepository;
import com.ridehailing.repository.DriverRepository;
import com.ridehailing.repository.RideRepository;
import com.ridehailing.repository.UserRepository;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.Executors;

import static org.assertj.core.api.Assertions.assertThat;

class ConcurrentBookingTest {

    @Test
    void concurrentUsersCannotReserveOneDriverTwice() throws Exception {
        UserService users = new UserService(new UserRepository());
        DriverRepository drivers = new DriverRepository();
        DriverService driverService = new DriverService(drivers);
        users.register(new RegisterUserRequest("one", "u1"));
        users.register(new RegisterUserRequest("two", "u2"));
        driverService.register(new RegisterDriverRequest("driver", "d", "reg", CarType.SEDAN));
        Driver driver = driverService.getRequired(1L);
        driver.setCurrentLocation(new Location(0, 0));
        driver.setStatus(DriverStatus.AVAILABLE);
        drivers.save(driver);
        RideService rides = new RideService(users, driverService, drivers, new RideRepository(),
                new DriverMatchingService(drivers, new NearestDriverStrategy(new DistanceCalculator()), 5),
                new PricingService((type, distance) -> BigDecimal.TEN),
                new CouponService(new CouponRepository()), new DistanceCalculator());

        var executor = Executors.newFixedThreadPool(2);
        try {
            List<Callable<Boolean>> calls = List.of(
                    () -> succeeds(rides, 1L),
                    () -> succeeds(rides, 2L));
            long successes = executor.invokeAll(calls).stream().filter(f -> {
                try {
                    return f.get();
                } catch (Exception e) {
                    return false;
                }
            }).count();
            assertThat(successes).isEqualTo(1);
            assertThat(drivers.findByStatus(DriverStatus.RESERVED)).hasSize(1);
        } finally {
            executor.shutdownNow();
        }
    }

    private boolean succeeds(RideService rides, long userId) {
        try {
            rides.requestRide(userId, new RequestRideRequest(0.0, 0.0, 1.0, 1.0,
                    CarType.SEDAN, null));
            return true;
        } catch (RuntimeException ignored) {
            return false;
        }
    }
}
