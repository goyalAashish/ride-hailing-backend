package com.ridehailing.repository;

import com.ridehailing.domain.Ride;
import com.ridehailing.domain.enums.RideStatus;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

@Repository
public class RideRepository extends InMemoryLongIdRepository<Ride> {

    @Override
    protected Long getId(Ride entity) {
        return entity.getRideId();
    }

    @Override
    protected void assignId(Ride entity, Long id) {
        entity.setRideId(id);
    }

    public List<Ride> findByUserId(Long userId) {
        return store.values().stream()
                .filter(ride -> userId.equals(ride.getUserId()))
                .toList();
    }

    public List<Ride> findByDriverId(Long driverId) {
        return store.values().stream()
                .filter(ride -> driverId.equals(ride.getDriverId()))
                .toList();
    }

    /**
     * At most one active ride is allowed per user; this returns that ride if present.
     */
    public Optional<Ride> findActiveByUserId(Long userId) {
        return findByUserId(userId).stream()
                .filter(ride -> ride.getStatus() != null && ride.getStatus().isActive())
                .findFirst();
    }

    public List<Ride> findByUserIdCreatedAfterNewestFirst(Long userId, LocalDateTime cutoff) {
        return findByUserId(userId).stream()
                .filter(ride -> ride.getCreatedAt() != null && !ride.getCreatedAt().isBefore(cutoff))
                .sorted(Comparator.comparing(Ride::getCreatedAt, Comparator.reverseOrder())
                        .thenComparing(Ride::getRideId, Comparator.reverseOrder()))
                .toList();
    }

    public List<Ride> findByDriverIdCreatedAfterNewestFirst(Long driverId, LocalDateTime cutoff) {
        return findByDriverId(driverId).stream()
                .filter(ride -> ride.getCreatedAt() != null && !ride.getCreatedAt().isBefore(cutoff))
                .sorted(Comparator.comparing(Ride::getCreatedAt, Comparator.reverseOrder())
                        .thenComparing(Ride::getRideId, Comparator.reverseOrder()))
                .toList();
    }

    public List<Ride> findByStatus(RideStatus status) {
        return store.values().stream()
                .filter(ride -> ride.getStatus() == status)
                .toList();
    }
}
