package com.ridehailing.service;

import com.ridehailing.domain.Driver;
import com.ridehailing.domain.Location;
import com.ridehailing.domain.enums.CarType;
import com.ridehailing.domain.enums.DriverStatus;
import com.ridehailing.domain.enums.RideStatus;
import com.ridehailing.dto.request.EndRideRequest;
import com.ridehailing.dto.request.RegisterDriverRequest;
import com.ridehailing.dto.request.RegisterUserRequest;
import com.ridehailing.dto.request.RequestRideRequest;
import com.ridehailing.exception.BadRequestException;
import com.ridehailing.matching.DistanceCalculator;
import com.ridehailing.matching.NearestDriverStrategy;
import com.ridehailing.repository.CouponRepository;
import com.ridehailing.repository.DriverRepository;
import com.ridehailing.repository.RideRepository;
import com.ridehailing.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.Executors;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class RideServiceTest {

    private RideService rideService;
    private DriverService driverService;
    private UserService userService;

    @BeforeEach
    void setUp() {
        userService = new UserService(new UserRepository());
        DriverRepository driverRepository = new DriverRepository();
        driverService = new DriverService(driverRepository);
        DriverMatchingService matchingService = new DriverMatchingService(
                driverRepository,
                new NearestDriverStrategy(new DistanceCalculator()),
                5.0);
        PricingService pricingService = new PricingService(
                (carType, distance) -> distance.multiply(
                        carType == CarType.HATCHBACK
                                ? new BigDecimal("10")
                                : new BigDecimal("20")));
        rideService = new RideService(
                userService,
                driverService,
                driverRepository,
                new RideRepository(),
                matchingService,
                pricingService,
                new CouponService(new CouponRepository()),
                new DistanceCalculator());

        userService.register(new RegisterUserRequest("Asha", "111"));
        driverService.register(new RegisterDriverRequest("Ravi", "222", "KA-1", CarType.SEDAN));
        driverService.register(new RegisterDriverRequest("Maya", "333", "KA-2", CarType.SEDAN));
        Driver driver = driverService.getRequired(1L);
        driver.setCurrentLocation(new Location(0, 0));
        driver.setStatus(DriverStatus.AVAILABLE);
        driverRepository.save(driver);
    }

    @Test
    void lifecycle_requestAcceptAndEnd_updatesRideAndDriverStates() {
        var requested = rideService.requestRide(
                1L,
                new RequestRideRequest(0.0, 0.0, 3.0, 4.0, CarType.SEDAN, null));

        assertThat(requested.status()).isEqualTo(RideStatus.REQUESTED);
        assertThat(requested.driverId()).isEqualTo(1L);
        assertThat(requested.finalPayableFare()).isEqualByComparingTo("100.00");

        var accepted = rideService.acceptRide(1L, requested.rideId());
        assertThat(accepted.status()).isEqualTo(RideStatus.ONGOING);
        assertThat(driverService.getRequired(1L).getStatus()).isEqualTo(DriverStatus.IN_RIDE_PICKUP);

        var completed = rideService.endRide(1L, requested.rideId(), new EndRideRequest(4.0, 5.0));
        assertThat(completed.status()).isEqualTo(RideStatus.COMPLETED);
        assertThat(completed.completedAt()).isNotNull();
        assertThat(completed.destinationLocation()).isEqualTo(new Location(4.0, 5.0));
        assertThat(driverService.getRequired(1L).getStatus()).isEqualTo(DriverStatus.AVAILABLE);
    }

    @Test
    void request_rejectsSecondActiveRideForSameUser() {
        rideService.requestRide(1L, new RequestRideRequest(0.0, 0.0, 1.0, 1.0, CarType.SEDAN, null));

        assertThatThrownBy(() -> rideService.requestRide(
                1L, new RequestRideRequest(0.0, 0.0, 2.0, 2.0, CarType.SEDAN, null)))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("active ride");
    }

    @Test
    void request_hatchbackFallsBackToSedanWithoutUpgradeSurcharge() {
        var requested = rideService.requestRide(
                1L,
                new RequestRideRequest(0.0, 0.0, 3.0, 4.0, CarType.HATCHBACK, null));

        assertThat(requested.requestedCarType()).isEqualTo(CarType.HATCHBACK);
        assertThat(requested.assignedCarType()).isEqualTo(CarType.SEDAN);
        assertThat(requested.finalPayableFare()).isEqualByComparingTo("50.00");
    }

    @Test
    void end_recalculatesFareUsingFinalDestination() {
        var ride = rideService.requestRide(
                1L, new RequestRideRequest(0.0, 0.0, 3.0, 4.0, CarType.SEDAN, null));
        rideService.acceptRide(1L, ride.rideId());

        var completed = rideService.endRide(
                1L, ride.rideId(), new EndRideRequest(6.0, 8.0));

        assertThat(completed.baseFare()).isEqualByComparingTo("200.00");
        assertThat(completed.finalPayableFare()).isEqualByComparingTo("200.00");
    }

    @Test
    void accept_rejectsDriverAlreadyBusyAndWrongDriver() {
        var first = rideService.requestRide(
                1L, new RequestRideRequest(0.0, 0.0, 1.0, 1.0, CarType.SEDAN, null));
        assertThatThrownBy(() -> rideService.acceptRide(2L, first.rideId()))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("another driver");

        rideService.acceptRide(1L, first.rideId());
        assertThatThrownBy(() -> rideService.acceptRide(1L, first.rideId()))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("requested");
    }

    @Test
    void end_rejectsAlreadyCompletedRide() {
        var ride = rideService.requestRide(
                1L, new RequestRideRequest(0.0, 0.0, 1.0, 1.0, CarType.SEDAN, null));
        rideService.acceptRide(1L, ride.rideId());
        rideService.endRide(1L, ride.rideId(), new EndRideRequest(1.0, 1.0));

        assertThatThrownBy(() -> rideService.endRide(1L, ride.rideId(), new EndRideRequest(2.0, 2.0)))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("ongoing");
    }

    @Test
    void accept_concurrentRequestsToSameDriver_allowOnlyOneRide() throws Exception {
        userService.register(new RegisterUserRequest("Maya", "112"));
        var first = rideService.requestRide(
                1L, new RequestRideRequest(0.0, 0.0, 1.0, 1.0, CarType.SEDAN, null));

        var executor = Executors.newFixedThreadPool(2);
        try {
            List<Callable<Boolean>> attempts = List.of(
                    () -> acceptSuccessfully(1L, first.rideId()),
                    () -> acceptSuccessfully(1L, first.rideId()));

            long successfulAccepts = executor.invokeAll(attempts).stream()
                    .filter(future -> {
                        try {
                            return future.get();
                        } catch (Exception exception) {
                            throw new AssertionError(exception);
                        }
                    })
                    .count();

            assertThat(successfulAccepts).isEqualTo(1);
        } finally {
            executor.shutdownNow();
        }
    }

    private boolean acceptSuccessfully(Long driverId, Long rideId) {
        try {
            rideService.acceptRide(driverId, rideId);
            return true;
        } catch (BadRequestException exception) {
            return false;
        }
    }
}
