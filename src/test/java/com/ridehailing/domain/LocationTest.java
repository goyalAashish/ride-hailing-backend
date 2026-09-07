package com.ridehailing.domain;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class LocationTest {

    @Test
    void equalCoordinates_areEqualValueObjects() {
        assertThat(new Location(1.5, 2.5)).isEqualTo(new Location(1.5, 2.5));
        assertThat(new Location(1.5, 2.5)).hasSameHashCodeAs(new Location(1.5, 2.5));
    }

    @Test
    void differentCoordinates_areNotEqual() {
        assertThat(new Location(0, 0)).isNotEqualTo(new Location(0, 1));
    }
}
