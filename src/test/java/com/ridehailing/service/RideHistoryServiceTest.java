package com.ridehailing.service;

import com.ridehailing.domain.Ride;
import com.ridehailing.dto.response.PageResponse;
import com.ridehailing.dto.response.RideResponse;
import com.ridehailing.exception.BadRequestException;
import com.ridehailing.repository.DriverRepository;
import com.ridehailing.repository.RideRepository;
import com.ridehailing.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class RideHistoryServiceTest {

    private RideHistoryService historyService;
    private RideRepository rideRepository;

    @BeforeEach
    void setUp() {
        UserService userService = new UserService(new UserRepository());
        DriverService driverService = new DriverService(new DriverRepository());
        rideRepository = new RideRepository();
        userService.register(new com.ridehailing.dto.request.RegisterUserRequest("Asha", "111"));
        driverService.register(new com.ridehailing.dto.request.RegisterDriverRequest(
                "Ravi", "222", "KA-1", com.ridehailing.domain.enums.CarType.SEDAN));
        historyService = new RideHistoryService(userService, driverService, rideRepository);
    }

    @Test
    void userHistory_filtersThirtyDaysAndPaginatesNewestFirst() {
        rideRepository.save(ride(1L, 1L, 1L, LocalDateTime.now().minusDays(1)));
        rideRepository.save(ride(2L, 1L, 1L, LocalDateTime.now().minusDays(2)));
        rideRepository.save(ride(3L, 1L, 1L, LocalDateTime.now().minusDays(31)));

        PageResponse<RideResponse> firstPage = historyService.userHistory(1L, 0, 1);
        PageResponse<RideResponse> secondPage = historyService.userHistory(1L, 1, 1);

        assertThat(firstPage.content()).extracting(RideResponse::rideId).containsExactly(1L);
        assertThat(secondPage.content()).extracting(RideResponse::rideId).containsExactly(2L);
        assertThat(firstPage.totalElements()).isEqualTo(2);
        assertThat(firstPage.totalPages()).isEqualTo(2);
    }

    @Test
    void driverHistory_filtersTwoDays() {
        rideRepository.save(ride(1L, 1L, 1L, LocalDateTime.now().minusHours(1)));
        rideRepository.save(ride(2L, 1L, 1L, LocalDateTime.now().minusDays(3)));

        PageResponse<RideResponse> history = historyService.driverHistory(1L, 0, 10);

        assertThat(history.content()).extracting(RideResponse::rideId).containsExactly(1L);
    }

    @Test
    void history_rejectsInvalidPageParameters() {
        assertThatThrownBy(() -> historyService.userHistory(1L, -1, 10))
                .isInstanceOf(BadRequestException.class);
        assertThatThrownBy(() -> historyService.userHistory(1L, 0, 0))
                .isInstanceOf(BadRequestException.class);
    }

    private Ride ride(Long id, Long userId, Long driverId, LocalDateTime createdAt) {
        Ride ride = new Ride();
        ride.setRideId(id);
        ride.setUserId(userId);
        ride.setDriverId(driverId);
        ride.setCreatedAt(createdAt);
        return ride;
    }
}
