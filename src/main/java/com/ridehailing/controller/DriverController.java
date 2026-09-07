package com.ridehailing.controller;

import com.ridehailing.dto.request.RegisterDriverRequest;
import com.ridehailing.dto.response.ApiResponse;
import com.ridehailing.dto.response.DriverResponse;
import com.ridehailing.service.DriverService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/drivers")
public class DriverController {

    private final DriverService driverService;

    public DriverController(DriverService driverService) {
        this.driverService = driverService;
    }

    @PostMapping
    public ResponseEntity<ApiResponse<DriverResponse>> register(@Valid @RequestBody RegisterDriverRequest request) {
        DriverResponse data = driverService.register(request);
        return ResponseEntity.ok(ApiResponse.success("Driver registered successfully", data));
    }
}
