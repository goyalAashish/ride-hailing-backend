package com.ridehailing.service;

import com.ridehailing.coupon.CouponDiscount;
import com.ridehailing.domain.Driver;
import com.ridehailing.domain.Location;
import com.ridehailing.domain.Ride;
import com.ridehailing.domain.enums.DriverStatus;
import com.ridehailing.domain.enums.RideStatus;
import com.ridehailing.dto.request.EndRideRequest;
import com.ridehailing.dto.request.RequestRideRequest;
import com.ridehailing.dto.response.RideResponse;
import com.ridehailing.exception.BadRequestException;
import com.ridehailing.exception.ResourceNotFoundException;
import com.ridehailing.matching.DistanceCalculator;
import com.ridehailing.repository.DriverRepository;
import com.ridehailing.repository.RideRepository;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

@Service
public class RideService {

    private static final BigDecimal ZERO_MONEY = new BigDecimal("0.00");

    private final UserService userService;
    private final DriverService driverService;
    private final DriverRepository driverRepository;
    private final RideRepository rideRepository;
    private final DriverMatchingService matchingService;
    private final PricingService pricingService;
    private final CouponService couponService;
    private final DistanceCalculator distanceCalculator;
    private final ConcurrentMap<Long, Object> userLocks = new ConcurrentHashMap<>();
    private final ConcurrentMap<Long, Object> driverLocks = new ConcurrentHashMap<>();
    private final Object driverReservationLock = new Object();

    public RideService(
            UserService userService,
            DriverService driverService,
            DriverRepository driverRepository,
            RideRepository rideRepository,
            DriverMatchingService matchingService,
            PricingService pricingService,
            CouponService couponService,
            DistanceCalculator distanceCalculator) {
        this.userService = userService;
        this.driverService = driverService;
        this.driverRepository = driverRepository;
        this.rideRepository = rideRepository;
        this.matchingService = matchingService;
        this.pricingService = pricingService;
        this.couponService = couponService;
        this.distanceCalculator = distanceCalculator;
    }

    public RideResponse requestRide(Long userId, RequestRideRequest request) {
        userService.getRequired(userId);
        Location pickup = location(request.pickupX(), request.pickupY());
        Location destination = location(request.destX(), request.destY());

        synchronized (lockFor(userLocks, userId)) {
            if (rideRepository.findActiveByUserId(userId).isPresent()) {
                throw new BadRequestException("ACTIVE_RIDE_EXISTS", "User already has an active ride");
            }

            Driver driver;
            synchronized (driverReservationLock) {
                driver = matchingService.findMatch(pickup, request.carType())
                        .orElseThrow(() -> new BadRequestException(
                                "NO_DRIVER_AVAILABLE", "No available driver found for requested car type"));
                if (driver.getStatus() != DriverStatus.AVAILABLE) {
                    throw new BadRequestException(
                            "DRIVER_UNAVAILABLE", "Selected driver is no longer available");
                }
                driver.setStatus(DriverStatus.RESERVED);
                driverRepository.save(driver);
            }

            BigDecimal baseFare = pricingService.calculateFare(
                    request.carType(),
                    BigDecimal.valueOf(distanceCalculator.between(pickup, destination)));
            BigDecimal discountAmount = ZERO_MONEY;
            String couponCode = null;
            if (request.couponCode() != null && !request.couponCode().isBlank()) {
                CouponDiscount discount = couponService.apply(userId, request.couponCode(), baseFare);
                couponCode = discount.couponCode();
                discountAmount = discount.discountAmount();
            }

            Ride ride = new Ride();
            ride.setUserId(userId);
            ride.setDriverId(driver.getDriverId());
            ride.setPickupLocation(pickup);
            ride.setDestinationLocation(destination);
            ride.setRequestedCarType(request.carType());
            ride.setAssignedCarType(driver.getCarType());
            ride.setStatus(RideStatus.REQUESTED);
            ride.setBaseFare(baseFare);
            ride.setAppliedCouponCode(couponCode);
            ride.setDiscountAmount(discountAmount);
            ride.setFinalPayableFare(
                    baseFare.subtract(discountAmount)
                            .max(ZERO_MONEY)
                            .setScale(2, RoundingMode.HALF_UP));
            ride.setCreatedAt(LocalDateTime.now());
            return RideResponse.from(rideRepository.save(ride));
        }
    }

    public RideResponse acceptRide(Long driverId, Long rideId) {
        Driver driver = driverService.getRequired(driverId);
        Ride ride = getRequired(rideId);

        synchronized (lockFor(driverLocks, driverId)) {
            if (!driverId.equals(ride.getDriverId())) {
                throw new BadRequestException("DRIVER_NOT_ASSIGNED", "Ride is assigned to another driver");
            }
            if (ride.getStatus() != RideStatus.REQUESTED) {
                throw new BadRequestException("INVALID_RIDE_STATE", "Only requested rides can be accepted");
            }
            if (driver.getStatus() != DriverStatus.RESERVED) {
                throw new BadRequestException("DRIVER_UNAVAILABLE", "Driver is not available");
            }

            ride.setStatus(RideStatus.ASSIGNED);
            rideRepository.save(ride);
            ride.setStatus(RideStatus.ONGOING);
            driver.setStatus(DriverStatus.IN_RIDE_PICKUP);
            driverRepository.save(driver);
            return RideResponse.from(rideRepository.save(ride));
        }
    }

    public RideResponse endRide(Long driverId, Long rideId, EndRideRequest request) {
        Driver driver = driverService.getRequired(driverId);
        Location destination = location(request.destX(), request.destY());
        Ride ride = getRequired(rideId);

        synchronized (lockFor(driverLocks, driverId)) {
            if (!driverId.equals(ride.getDriverId())) {
                throw new BadRequestException("DRIVER_NOT_ASSIGNED", "Ride is assigned to another driver");
            }
            if (ride.getStatus() != RideStatus.ONGOING) {
                throw new BadRequestException("INVALID_RIDE_STATE", "Only ongoing rides can be completed");
            }

            ride.setDestinationLocation(destination);
            BigDecimal actualDistance = BigDecimal.valueOf(
                    distanceCalculator.between(ride.getPickupLocation(), destination));
            BigDecimal adjustedBaseFare = pricingService.calculateFare(
                    ride.getRequestedCarType(), actualDistance);
            BigDecimal discountAmount = ZERO_MONEY;
            if (ride.getAppliedCouponCode() != null) {
                discountAmount = couponService.calculateDiscount(
                        ride.getUserId(), ride.getAppliedCouponCode(), adjustedBaseFare);
            }
            ride.setBaseFare(adjustedBaseFare);
            ride.setDiscountAmount(discountAmount);
            ride.setFinalPayableFare(
                    adjustedBaseFare.subtract(discountAmount)
                            .max(ZERO_MONEY)
                            .setScale(2, RoundingMode.HALF_UP));
            ride.setStatus(RideStatus.COMPLETED);
            ride.setCompletedAt(LocalDateTime.now());
            driver.setCurrentLocation(destination);
            driver.setStatus(DriverStatus.AVAILABLE);
            driverRepository.save(driver);
            return RideResponse.from(rideRepository.save(ride));
        }
    }

    private Ride getRequired(Long rideId) {
        return rideRepository.findById(rideId)
                .orElseThrow(() -> new ResourceNotFoundException("Ride", rideId));
    }

    private Location location(double x, double y) {
        if (!Double.isFinite(x) || !Double.isFinite(y)) {
            throw new BadRequestException("INVALID_LOCATION", "Location coordinates must be finite");
        }
        return new Location(x, y);
    }

    private Object lockFor(ConcurrentMap<Long, Object> locks, Long id) {
        return locks.computeIfAbsent(id, ignored -> new Object());
    }

}
