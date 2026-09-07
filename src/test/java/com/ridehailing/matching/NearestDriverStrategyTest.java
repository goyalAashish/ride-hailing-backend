package com.ridehailing.matching;

import com.ridehailing.domain.Driver;
import com.ridehailing.domain.Location;
import com.ridehailing.domain.Vehicle;
import com.ridehailing.domain.enums.CarType;
import com.ridehailing.domain.enums.DriverStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class NearestDriverStrategyTest {

    private NearestDriverStrategy strategy;

    @BeforeEach
    void setUp() {
        strategy = new NearestDriverStrategy(new DistanceCalculator());
    }

    @Test
    void match_returnsNearestEligibleDriverWithinRadius() {
        Driver farther = driver(1L, CarType.SEDAN, new Location(3, 4));
        Driver nearest = driver(2L, CarType.SEDAN, new Location(1, 1));
        Driver wrongType = driver(3L, CarType.HATCHBACK, new Location(0, 0));
        Driver offline = driver(4L, CarType.SEDAN, new Location(0, 0));
        offline.setStatus(DriverStatus.OFFLINE);

        assertThat(strategy.match(
                new Location(0, 0),
                CarType.SEDAN,
                List.of(farther, nearest, wrongType, offline),
                5.0))
                .containsSame(nearest);
    }

    @Test
    void match_includesDriverOnRadiusBoundary() {
        Driver driver = driver(1L, CarType.SEDAN, new Location(3, 4));

        assertThat(strategy.match(new Location(0, 0), CarType.SEDAN, List.of(driver), 5.0))
                .containsSame(driver);
    }

    @Test
    void match_breaksEqualDistanceTiesByDriverId() {
        Driver lowerId = driver(1L, CarType.SEDAN, new Location(1, 0));
        Driver higherId = driver(2L, CarType.SEDAN, new Location(-1, 0));

        assertThat(strategy.match(
                new Location(0, 0), CarType.SEDAN, List.of(higherId, lowerId), 5.0))
                .containsSame(lowerId);
    }

    @Test
    void match_returnsEmptyWhenNoDriverHasLocationOrIsNearby() {
        Driver missingLocation = driver(1L, CarType.SEDAN, null);
        Driver outsideRadius = driver(2L, CarType.SEDAN, new Location(6, 0));

        assertThat(strategy.match(
                new Location(0, 0),
                CarType.SEDAN,
                List.of(missingLocation, outsideRadius),
                5.0))
                .isEmpty();
    }

    @Test
    void match_rejectsInvalidRadius() {
        assertThatThrownBy(() -> strategy.match(
                new Location(0, 0), CarType.SEDAN, List.of(), -1.0))
                .isInstanceOf(IllegalArgumentException.class);
    }

    private static Driver driver(Long id, CarType carType, Location location) {
        Driver driver = new Driver("Driver " + id, String.valueOf(id), new Vehicle("REG-" + id, carType));
        driver.setDriverId(id);
        driver.setStatus(DriverStatus.AVAILABLE);
        driver.setCurrentLocation(location);
        return driver;
    }
}
