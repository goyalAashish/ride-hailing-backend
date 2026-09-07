package com.ridehailing.service;

import com.ridehailing.domain.enums.CarType;
import com.ridehailing.domain.enums.DriverStatus;
import com.ridehailing.dto.request.RegisterDriverRequest;
import com.ridehailing.dto.response.DriverResponse;
import com.ridehailing.exception.DuplicateResourceException;
import com.ridehailing.exception.ResourceNotFoundException;
import com.ridehailing.repository.DriverRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class DriverServiceTest {

    private DriverService driverService;

    @BeforeEach
    void setUp() {
        driverService = new DriverService(new DriverRepository());
    }

    @Test
    void register_createsOfflineDriverWithNormalizedPlate() {
        DriverResponse response = driverService.register(
                new RegisterDriverRequest("  Ravi  ", " 888 ", " ka-01-aa-1111 ", CarType.SEDAN));

        assertThat(response.driverId()).isEqualTo(1L);
        assertThat(response.name()).isEqualTo("Ravi");
        assertThat(response.phone()).isEqualTo("888");
        assertThat(response.registrationNumber()).isEqualTo("KA-01-AA-1111");
        assertThat(response.carType()).isEqualTo(CarType.SEDAN);
        assertThat(response.status()).isEqualTo(DriverStatus.OFFLINE);
    }

    @Test
    void register_rejectsDuplicatePhone() {
        driverService.register(new RegisterDriverRequest("Ravi", "888", "KA-1", CarType.HATCHBACK));

        assertThatThrownBy(() -> driverService.register(
                new RegisterDriverRequest("Other", "888", "KA-2", CarType.SEDAN)))
                .isInstanceOf(DuplicateResourceException.class)
                .hasMessageContaining("phone");
    }

    @Test
    void register_rejectsDuplicateRegistrationRegardlessOfCase() {
        driverService.register(new RegisterDriverRequest("Ravi", "888", "KA-01-AA-1111", CarType.HATCHBACK));

        assertThatThrownBy(() -> driverService.register(
                new RegisterDriverRequest("Other", "777", "ka-01-aa-1111", CarType.SEDAN)))
                .isInstanceOf(DuplicateResourceException.class)
                .hasMessageContaining("registrationNumber");
    }

    @Test
    void getRequired_throwsWhenMissing() {
        assertThatThrownBy(() -> driverService.getRequired(9L))
                .isInstanceOf(ResourceNotFoundException.class);
    }
}
