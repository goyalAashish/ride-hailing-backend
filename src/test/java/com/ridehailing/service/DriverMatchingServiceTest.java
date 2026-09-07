package com.ridehailing.service;

import com.ridehailing.domain.Driver;
import com.ridehailing.domain.Location;
import com.ridehailing.domain.Vehicle;
import com.ridehailing.domain.enums.CarType;
import com.ridehailing.domain.enums.DriverStatus;
import com.ridehailing.matching.DistanceCalculator;
import com.ridehailing.matching.NearestDriverStrategy;
import com.ridehailing.repository.DriverRepository;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class DriverMatchingServiceTest {

    @Test
    void findMatch_usesConfiguredRadiusAndAvailableDrivers() {
        DriverRepository repository = new DriverRepository();
        Driver driver = new Driver("Ravi", "888", new Vehicle("KA-1", CarType.SEDAN));
        driver.setStatus(DriverStatus.AVAILABLE);
        driver.setCurrentLocation(new Location(3, 4));
        repository.save(driver);

        DriverMatchingService service = new DriverMatchingService(
                repository,
                new NearestDriverStrategy(new DistanceCalculator()),
                5.0);

        assertThat(service.findMatch(new Location(0, 0), CarType.SEDAN))
                .containsSame(driver);
    }
}
