package com.ridehailing.service;

import com.ridehailing.domain.Driver;
import com.ridehailing.domain.Location;
import com.ridehailing.domain.Vehicle;
import com.ridehailing.domain.enums.DriverStatus;
import com.ridehailing.dto.request.RegisterDriverRequest;
import com.ridehailing.dto.response.DriverResponse;
import com.ridehailing.exception.BadRequestException;
import com.ridehailing.exception.DuplicateResourceException;
import com.ridehailing.exception.ResourceNotFoundException;
import com.ridehailing.repository.DriverRepository;
import org.springframework.stereotype.Service;

@Service
public class DriverService {

    private final DriverRepository driverRepository;

    public DriverService(DriverRepository driverRepository) {
        this.driverRepository = driverRepository;
    }

    public DriverResponse register(RegisterDriverRequest request) {
        String name = request.name().trim();
        String phone = request.phone().trim();
        String registrationNumber = request.registrationNumber().trim().toUpperCase();

        if (driverRepository.findByPhone(phone).isPresent()) {
            throw new DuplicateResourceException("Driver", "phone", phone);
        }
        if (driverRepository.findByRegistrationNumber(registrationNumber).isPresent()) {
            throw new DuplicateResourceException("Driver", "registrationNumber", registrationNumber);
        }

        Vehicle vehicle = new Vehicle(registrationNumber, request.carType());
        Driver saved = driverRepository.save(new Driver(name, phone, vehicle));
        return DriverResponse.from(saved);
    }

    public Driver getRequired(Long driverId) {
        return driverRepository.findById(driverId)
                .orElseThrow(() -> new ResourceNotFoundException("Driver", driverId));
    }

    public void markAvailable(Long driverId) {
        Driver driver = getRequired(driverId);
        synchronized (driver) {
            driver.setStatus(DriverStatus.AVAILABLE);
        }
    }

    public void markOffline(Long driverId) {
        Driver driver = getRequired(driverId);
        synchronized (driver) {
            driver.setStatus(DriverStatus.OFFLINE);
        }
    }

    public DriverResponse updateLocation(Long driverId, double x, double y) {
        if (!Double.isFinite(x) || !Double.isFinite(y)) {
            throw new BadRequestException("INVALID_LOCATION", "Location coordinates must be finite");
        }
        Driver driver = getRequired(driverId);
        synchronized (driver) {
            driver.setCurrentLocation(new Location(x, y));
            return DriverResponse.from(driverRepository.save(driver));
        }
    }
}
