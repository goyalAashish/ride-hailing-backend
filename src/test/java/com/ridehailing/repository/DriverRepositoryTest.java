package com.ridehailing.repository;

import com.ridehailing.domain.Driver;
import com.ridehailing.domain.Vehicle;
import com.ridehailing.domain.enums.CarType;
import com.ridehailing.domain.enums.DriverStatus;
import com.ridehailing.exception.DuplicateResourceException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class DriverRepositoryTest {

    private DriverRepository repository;

    @BeforeEach
    void setUp() {
        repository = new DriverRepository();
    }

    @Test
    void save_assignsIdAndLinksVehicle() {
        Driver driver = repository.save(driver("111", "KA-01-AA-1111", CarType.SEDAN));

        assertThat(driver.getDriverId()).isEqualTo(1L);
        assertThat(driver.getStatus()).isEqualTo(DriverStatus.OFFLINE);
        assertThat(driver.getVehicle().getDriverId()).isEqualTo(1L);
        assertThat(driver.getCarType()).isEqualTo(CarType.SEDAN);
    }

    @Test
    void save_rejectsDuplicatePhone() {
        repository.save(driver("111", "KA-01-AA-1111", CarType.HATCHBACK));

        assertThatThrownBy(() -> repository.save(driver("111", "KA-01-AA-2222", CarType.SEDAN)))
                .isInstanceOf(DuplicateResourceException.class)
                .hasMessageContaining("phone");
    }

    @Test
    void save_rejectsDuplicateRegistrationAndDoesNotLeakPhoneIndex() {
        repository.save(driver("111", "KA-01-AA-1111", CarType.HATCHBACK));

        assertThatThrownBy(() -> repository.save(driver("222", "KA-01-AA-1111", CarType.SEDAN)))
                .isInstanceOf(DuplicateResourceException.class)
                .hasMessageContaining("registrationNumber");

        Driver other = repository.save(driver("222", "KA-01-AA-9999", CarType.SUV));
        assertThat(other.getDriverId()).isEqualTo(3L);
        assertThat(repository.findByPhone("222")).contains(other);
    }

    @Test
    void findByStatusAndCarType_filtersAvailableSedans() {
        Driver availableSedan = driver("111", "KA-1", CarType.SEDAN);
        availableSedan.setStatus(DriverStatus.AVAILABLE);
        repository.save(availableSedan);

        Driver offlineSedan = driver("222", "KA-2", CarType.SEDAN);
        repository.save(offlineSedan);

        Driver availableHatch = driver("333", "KA-3", CarType.HATCHBACK);
        availableHatch.setStatus(DriverStatus.AVAILABLE);
        repository.save(availableHatch);

        assertThat(repository.findByStatusAndCarType(DriverStatus.AVAILABLE, CarType.SEDAN))
                .containsExactly(availableSedan);
    }

    @Test
    void deleteById_releasesUniqueIndexes() {
        Driver driver = repository.save(driver("111", "KA-01-AA-1111", CarType.SEDAN));
        repository.deleteById(driver.getDriverId());

        Driver reused = repository.save(driver("111", "KA-01-AA-1111", CarType.SEDAN));
        assertThat(reused.getDriverId()).isEqualTo(2L);
    }

    private static Driver driver(String phone, String registration, CarType carType) {
        return new Driver("Driver " + phone, phone, new Vehicle(registration, carType));
    }
}
