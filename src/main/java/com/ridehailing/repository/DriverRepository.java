package com.ridehailing.repository;

import com.ridehailing.domain.Driver;
import com.ridehailing.domain.enums.CarType;
import com.ridehailing.domain.enums.DriverStatus;
import com.ridehailing.exception.DuplicateResourceException;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

@Repository
public class DriverRepository extends InMemoryLongIdRepository<Driver> {

    private final ConcurrentMap<String, Long> phoneIndex = new ConcurrentHashMap<>();
    private final ConcurrentMap<String, Long> registrationIndex = new ConcurrentHashMap<>();

    @Override
    protected Long getId(Driver entity) {
        return entity.getDriverId();
    }

    @Override
    protected void assignId(Driver entity, Long id) {
        entity.setDriverId(id);
    }

    @Override
    public Driver save(Driver driver) {
        Objects.requireNonNull(driver, "driver is required");
        Objects.requireNonNull(driver.getPhone(), "phone is required");
        Objects.requireNonNull(driver.getRegistrationNumber(), "registrationNumber is required");

        Long id = driver.getDriverId() == null ? nextId() : driver.getDriverId();

        Long phoneOwner = phoneIndex.putIfAbsent(driver.getPhone(), id);
        if (phoneOwner != null && !phoneOwner.equals(id)) {
            throw new DuplicateResourceException("Driver", "phone", driver.getPhone());
        }

        Long registrationOwner = registrationIndex.putIfAbsent(driver.getRegistrationNumber(), id);
        if (registrationOwner != null && !registrationOwner.equals(id)) {
            if (phoneOwner == null) {
                phoneIndex.remove(driver.getPhone(), id);
            }
            throw new DuplicateResourceException("Driver", "registrationNumber", driver.getRegistrationNumber());
        }

        driver.setDriverId(id);
        store.put(id, driver);
        return driver;
    }

    public Optional<Driver> findByPhone(String phone) {
        if (phone == null) {
            return Optional.empty();
        }
        Long id = phoneIndex.get(phone);
        return id == null ? Optional.empty() : findById(id);
    }

    public Optional<Driver> findByRegistrationNumber(String registrationNumber) {
        if (registrationNumber == null) {
            return Optional.empty();
        }
        Long id = registrationIndex.get(registrationNumber);
        return id == null ? Optional.empty() : findById(id);
    }

    public List<Driver> findByStatusAndCarType(DriverStatus status, CarType carType) {
        return store.values().stream()
                .filter(driver -> driver.getStatus() == status && driver.getCarType() == carType)
                .toList();
    }

    public List<Driver> findByStatus(DriverStatus status) {
        return store.values().stream()
                .filter(driver -> driver.getStatus() == status)
                .toList();
    }

    @Override
    public boolean deleteById(Long id) {
        findById(id).ifPresent(driver -> {
            phoneIndex.remove(driver.getPhone(), id);
            registrationIndex.remove(driver.getRegistrationNumber(), id);
        });
        return super.deleteById(id);
    }

    @Override
    protected void onCleared() {
        super.onCleared();
        phoneIndex.clear();
        registrationIndex.clear();
    }
}
