package com.ridehailing.controller;

import com.ridehailing.dto.request.RegisterDriverRequest;
import com.ridehailing.dto.response.ApiResponse;
import com.ridehailing.dto.response.DriverResponse;
import com.ridehailing.dto.response.PageResponse;
import com.ridehailing.dto.response.RideResponse;
import com.ridehailing.service.DriverService;
import com.ridehailing.service.RideHistoryService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/drivers")
public class DriverController {

    private final DriverService driverService;
    private final RideHistoryService rideHistoryService;

    public DriverController(DriverService driverService, RideHistoryService rideHistoryService) {
        this.driverService = driverService;
        this.rideHistoryService = rideHistoryService;
    }

    @PostMapping
    public ResponseEntity<ApiResponse<DriverResponse>> register(@Valid @RequestBody RegisterDriverRequest request) {
        DriverResponse data = driverService.register(request);
        return ResponseEntity.ok(ApiResponse.success("Driver registered successfully", data));
    }

    @GetMapping("/rides/history")
    public ResponseEntity<ApiResponse<PageResponse<RideResponse>>> history(
            @RequestHeader("X-Driver-Id") Long driverId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        return ResponseEntity.ok(ApiResponse.success(
                "Driver ride history retrieved successfully",
                rideHistoryService.driverHistory(driverId, page, size)));
    }
}
