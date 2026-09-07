package com.ridehailing.repository;

import com.ridehailing.domain.Location;
import com.ridehailing.domain.Ride;
import com.ridehailing.domain.enums.CarType;
import com.ridehailing.domain.enums.RideStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

class RideRepositoryTest {

    private RideRepository repository;

    @BeforeEach
    void setUp() {
        repository = new RideRepository();
    }

    @Test
    void save_assignsIdAndDefaultsToRequested() {
        Ride ride = ride(10L, RideStatus.REQUESTED, LocalDateTime.now());
        repository.save(ride);

        assertThat(ride.getRideId()).isEqualTo(1L);
        assertThat(ride.getStatus()).isEqualTo(RideStatus.REQUESTED);
        assertThat(ride.getPickupLocation()).isEqualTo(new Location(0, 0));
    }

    @Test
    void findActiveByUserId_ignoresCompletedRides() {
        Ride completed = ride(10L, RideStatus.COMPLETED, LocalDateTime.now().minusDays(1));
        repository.save(completed);
        Ride active = ride(10L, RideStatus.ONGOING, LocalDateTime.now());
        repository.save(active);
        repository.save(ride(11L, RideStatus.REQUESTED, LocalDateTime.now()));

        assertThat(repository.findActiveByUserId(10L)).contains(active);
        assertThat(repository.findActiveByUserId(99L)).isEmpty();
    }

    @Test
    void findByUserIdCreatedAfterNewestFirst_appliesCutoffAndOrder() {
        LocalDateTime now = LocalDateTime.of(2026, 9, 7, 12, 0);
        Ride older = ride(10L, RideStatus.COMPLETED, now.minusDays(40));
        Ride mid = ride(10L, RideStatus.COMPLETED, now.minusDays(5));
        Ride newest = ride(10L, RideStatus.COMPLETED, now.minusDays(1));
        repository.save(older);
        repository.save(mid);
        repository.save(newest);

        assertThat(repository.findByUserIdCreatedAfterNewestFirst(10L, now.minusDays(30)))
                .extracting(Ride::getRideId)
                .containsExactly(newest.getRideId(), mid.getRideId());
    }

    @Test
    void findByDriverIdCreatedAfterNewestFirst_filtersAssignedDriver() {
        LocalDateTime now = LocalDateTime.of(2026, 9, 7, 12, 0);
        Ride mine = ride(10L, RideStatus.COMPLETED, now.minusHours(2));
        mine.setDriverId(5L);
        Ride other = ride(11L, RideStatus.COMPLETED, now.minusHours(1));
        other.setDriverId(6L);
        repository.save(mine);
        repository.save(other);

        assertThat(repository.findByDriverIdCreatedAfterNewestFirst(5L, now.minusDays(2)))
                .containsExactly(mine);
    }

    private static Ride ride(Long userId, RideStatus status, LocalDateTime createdAt) {
        Ride ride = new Ride();
        ride.setUserId(userId);
        ride.setStatus(status);
        ride.setRequestedCarType(CarType.SEDAN);
        ride.setPickupLocation(new Location(0, 0));
        ride.setDestinationLocation(new Location(3, 4));
        ride.setCreatedAt(createdAt);
        return ride;
    }
}
