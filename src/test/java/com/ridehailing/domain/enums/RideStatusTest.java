package com.ridehailing.domain.enums;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class RideStatusTest {

    @Test
    void requestedAssignedAndOngoing_areActive() {
        assertThat(RideStatus.REQUESTED.isActive()).isTrue();
        assertThat(RideStatus.ASSIGNED.isActive()).isTrue();
        assertThat(RideStatus.ONGOING.isActive()).isTrue();
        assertThat(RideStatus.COMPLETED.isActive()).isFalse();
        assertThat(RideStatus.CANCELLED.isActive()).isFalse();
    }

    @Test
    void completedAndCancelled_areTerminal() {
        assertThat(RideStatus.COMPLETED.isTerminal()).isTrue();
        assertThat(RideStatus.CANCELLED.isTerminal()).isTrue();
        assertThat(RideStatus.ONGOING.isTerminal()).isFalse();
    }
}
