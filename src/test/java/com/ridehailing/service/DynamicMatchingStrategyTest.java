package com.ridehailing.service;

import com.ridehailing.domain.Driver;
import com.ridehailing.domain.Location;
import com.ridehailing.domain.Vehicle;
import com.ridehailing.domain.enums.CarType;
import com.ridehailing.domain.enums.DriverStatus;
import com.ridehailing.matching.DistanceCalculator;
import com.ridehailing.matching.HighestRatedDriverStrategy;
import com.ridehailing.matching.NearestDriverStrategy;
import com.ridehailing.repository.DriverRepository;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class DynamicMatchingStrategyTest {

    @Test
    void strategyCanBeSwitchedAtRuntime() {
        DriverRepository repository = new DriverRepository();
        Driver near = driver("near", "1", 0, 1);
        Driver rated = driver("rated", "2", 0, 4);
        near.addRating(1);
        near.addRating(1);
        repository.save(near);
        repository.save(rated);
        DriverMatchingService service = new DriverMatchingService(repository,
                new NearestDriverStrategy(new DistanceCalculator()),
                new HighestRatedDriverStrategy(new DistanceCalculator()), 10);

        assertThat(service.findMatch(new Location(0, 0), CarType.SEDAN)).containsSame(near);
        service.setStrategy("highest_rated");
        assertThat(service.getStrategyName()).isEqualTo("HIGHEST_RATED");
        assertThat(service.findMatch(new Location(0, 0), CarType.SEDAN)).containsSame(rated);
    }

    private Driver driver(String name, String phone, double x, double y) {
        Driver driver = new Driver(name, phone, new Vehicle(phone, CarType.SEDAN));
        driver.setCurrentLocation(new Location(x, y));
        driver.setStatus(DriverStatus.AVAILABLE);
        return driver;
    }
}
