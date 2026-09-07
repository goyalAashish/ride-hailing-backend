package com.ridehailing.service;

import com.ridehailing.domain.Driver;
import com.ridehailing.domain.Vehicle;
import com.ridehailing.dto.request.RegisterDriverRequest;
import com.ridehailing.dto.response.DriverResponse;
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
}
