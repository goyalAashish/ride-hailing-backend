package com.ridehailing.controller;

import com.ridehailing.dto.request.MatchingStrategyRequest;
import com.ridehailing.dto.response.ApiResponse;
import com.ridehailing.service.DriverMatchingService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/v1/admin/matching-strategy")
public class AdminMatchingController {

    private final DriverMatchingService matchingService;

    public AdminMatchingController(DriverMatchingService matchingService) {
        this.matchingService = matchingService;
    }

    @GetMapping
    public ResponseEntity<ApiResponse<Map<String, String>>> current() {
        return ResponseEntity.ok(ApiResponse.success("Matching strategy retrieved successfully",
                Map.of("strategy", matchingService.getStrategyName())));
    }

    @org.springframework.web.bind.annotation.RequestMapping(method = {RequestMethod.PUT, RequestMethod.POST})
    public ResponseEntity<ApiResponse<Map<String, String>>> update(
            @Valid @RequestBody MatchingStrategyRequest request) {
        matchingService.setStrategy(request.strategy());
        return current();
    }

    @org.springframework.web.bind.annotation.RequestMapping(value = "/{strategy}",
            method = {RequestMethod.PUT, RequestMethod.POST})
    public ResponseEntity<ApiResponse<Map<String, String>>> updatePath(
            @org.springframework.web.bind.annotation.PathVariable String strategy) {
        matchingService.setStrategy(strategy);
        return current();
    }
}
