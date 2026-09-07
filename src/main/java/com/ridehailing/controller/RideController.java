package com.ridehailing.controller;

import com.ridehailing.dto.request.EndRideRequest;
import com.ridehailing.dto.request.RequestRideRequest;
import com.ridehailing.dto.response.ApiResponse;
import com.ridehailing.dto.response.RideResponse;
import com.ridehailing.service.RideService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/rides")
public class RideController {

    private final RideService rideService;

    public RideController(RideService rideService) {
        this.rideService = rideService;
    }

    @PostMapping("/request")
    public ResponseEntity<ApiResponse<RideResponse>> requestRide(
            @RequestHeader("X-User-Id") Long userId,
            @Valid @RequestBody RequestRideRequest request) {
        return ResponseEntity.ok(ApiResponse.success(
                "Ride requested successfully",
                rideService.requestRide(userId, request)));
    }

    @PostMapping("/{rideId}/accept")
    public ResponseEntity<ApiResponse<RideResponse>> acceptRide(
            @RequestHeader("X-Driver-Id") Long driverId,
            @PathVariable Long rideId) {
        return ResponseEntity.ok(ApiResponse.success(
                "Ride accepted successfully",
                rideService.acceptRide(driverId, rideId)));
    }

    @PostMapping("/{rideId}/end")
    public ResponseEntity<ApiResponse<RideResponse>> endRide(
            @RequestHeader("X-Driver-Id") Long driverId,
            @PathVariable Long rideId,
            @Valid @RequestBody EndRideRequest request) {
        return ResponseEntity.ok(ApiResponse.success(
                "Ride completed successfully",
                rideService.endRide(driverId, rideId, request)));
    }
}
