package com.ridehailing.domain.enums;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class DriverStatusTest {

    @Test
    void onlyAvailable_isMatchable() {
        assertThat(DriverStatus.AVAILABLE.isAvailableForMatching()).isTrue();
        assertThat(DriverStatus.OFFLINE.isAvailableForMatching()).isFalse();
        assertThat(DriverStatus.IN_RIDE_PICKUP.isAvailableForMatching()).isFalse();
    }

    @Test
    void inRideStates_areOnTrip() {
        assertThat(DriverStatus.IN_RIDE_PICKUP.isOnTrip()).isTrue();
        assertThat(DriverStatus.IN_RIDE_ONROUTE.isOnTrip()).isTrue();
        assertThat(DriverStatus.IN_RIDE_ARRIVED.isOnTrip()).isTrue();
        assertThat(DriverStatus.AVAILABLE.isOnTrip()).isFalse();
        assertThat(DriverStatus.OFFLINE.isOnTrip()).isFalse();
    }
}
