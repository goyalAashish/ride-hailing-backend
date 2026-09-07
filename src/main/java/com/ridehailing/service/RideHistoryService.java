package com.ridehailing.service;

import com.ridehailing.domain.Ride;
import com.ridehailing.dto.response.PageResponse;
import com.ridehailing.dto.response.RideResponse;
import com.ridehailing.exception.BadRequestException;
import com.ridehailing.repository.RideRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class RideHistoryService {

    private static final int USER_HISTORY_DAYS = 30;
    private static final int DRIVER_HISTORY_DAYS = 2;

    private final UserService userService;
    private final DriverService driverService;
    private final RideRepository rideRepository;

    public RideHistoryService(
            UserService userService,
            DriverService driverService,
            RideRepository rideRepository) {
        this.userService = userService;
        this.driverService = driverService;
        this.rideRepository = rideRepository;
    }

    public PageResponse<RideResponse> userHistory(Long userId, int page, int size) {
        userService.getRequired(userId);
        validatePage(page, size);

        List<Ride> rides = rideRepository.findByUserIdCreatedAfterNewestFirst(
                userId, LocalDateTime.now().minusDays(USER_HISTORY_DAYS));
        return paginate(rides, page, size);
    }

    public PageResponse<RideResponse> driverHistory(Long driverId, int page, int size) {
        driverService.getRequired(driverId);
        validatePage(page, size);

        List<Ride> rides = rideRepository.findByDriverIdCreatedAfterNewestFirst(
                driverId, LocalDateTime.now().minusDays(DRIVER_HISTORY_DAYS));
        return paginate(rides, page, size);
    }

    private PageResponse<RideResponse> paginate(List<Ride> rides, int page, int size) {
        long totalElements = rides.size();
        int totalPages = totalElements == 0 ? 0 : (int) ((totalElements + size - 1) / size);
        long offset = (long) page * size;
        int fromIndex = offset >= totalElements ? rides.size() : (int) offset;
        int toIndex = Math.min(fromIndex + size, rides.size());

        List<RideResponse> content = rides.subList(fromIndex, toIndex).stream()
                .map(RideResponse::from)
                .toList();
        boolean first = page == 0;
        boolean last = totalPages == 0 || page >= totalPages - 1;
        return new PageResponse<>(content, page, size, totalElements, totalPages, first, last);
    }

    private void validatePage(int page, int size) {
        if (page < 0) {
            throw new BadRequestException("INVALID_PAGE", "Page must be non-negative");
        }
        if (size <= 0) {
            throw new BadRequestException("INVALID_PAGE_SIZE", "Page size must be greater than zero");
        }
    }
}
