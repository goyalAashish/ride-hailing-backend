package com.ridehailing.controller;

import com.ridehailing.dto.request.RegisterUserRequest;
import com.ridehailing.dto.response.ApiResponse;
import com.ridehailing.dto.response.PageResponse;
import com.ridehailing.dto.response.RideResponse;
import com.ridehailing.dto.response.UserResponse;
import com.ridehailing.service.RideHistoryService;
import com.ridehailing.service.UserService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/users")
public class UserController {

    private final UserService userService;
    private final RideHistoryService rideHistoryService;

    public UserController(UserService userService, RideHistoryService rideHistoryService) {
        this.userService = userService;
        this.rideHistoryService = rideHistoryService;
    }

    @PostMapping
    public ResponseEntity<ApiResponse<UserResponse>> register(@Valid @RequestBody RegisterUserRequest request) {
        UserResponse data = userService.register(request);
        return ResponseEntity.ok(ApiResponse.success("User registered successfully", data));
    }

    @GetMapping("/rides/history")
    public ResponseEntity<ApiResponse<PageResponse<RideResponse>>> history(
            @RequestHeader("X-User-Id") Long userId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        return ResponseEntity.ok(ApiResponse.success(
                "User ride history retrieved successfully",
                rideHistoryService.userHistory(userId, page, size)));
    }
}
