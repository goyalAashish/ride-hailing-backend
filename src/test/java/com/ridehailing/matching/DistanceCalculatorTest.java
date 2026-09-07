package com.ridehailing.matching;

import com.ridehailing.domain.Location;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class DistanceCalculatorTest {

    @Test
    void between_usesEuclideanDistance() {
        assertThat(new DistanceCalculator().between(new Location(0, 0), new Location(3, 4)))
                .isEqualTo(5.0);
    }
}
