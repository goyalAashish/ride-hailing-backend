package com.ridehailing.controller;

import com.ridehailing.dto.request.RegisterUserRequest;
import com.ridehailing.dto.response.ApiResponse;
import com.ridehailing.dto.response.UserResponse;
import com.ridehailing.service.UserService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/users")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @PostMapping
    public ResponseEntity<ApiResponse<UserResponse>> register(@Valid @RequestBody RegisterUserRequest request) {
        UserResponse data = userService.register(request);
        return ResponseEntity.ok(ApiResponse.success("User registered successfully", data));
    }
}
