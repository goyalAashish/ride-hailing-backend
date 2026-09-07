package com.ridehailing.controller;

import com.ridehailing.dto.request.AreaDemandSupplyRequest;
import com.ridehailing.dto.response.ApiResponse;
import com.ridehailing.surge.SurgePricingService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/admin/surge/areas")
public class AdminSurgeController {

    private final SurgePricingService surgePricingService;

    public AdminSurgeController(SurgePricingService surgePricingService) {
        this.surgePricingService = surgePricingService;
    }

    @PutMapping("/{area}")
    public ResponseEntity<ApiResponse<Map<String, Object>>> update(
            @PathVariable String area, @Valid @RequestBody AreaDemandSupplyRequest request) {
        surgePricingService.setDemandSupply(area, request.demand(), request.supply());
        return current(area);
    }

    @GetMapping("/{area}")
    public ResponseEntity<ApiResponse<Map<String, Object>>> current(@PathVariable String area) {
        BigDecimal ratio = surgePricingService.getDemandSupplyRatio(area);
        return ResponseEntity.ok(ApiResponse.success("Area surge data retrieved successfully",
                Map.of("area", area, "demandSupplyRatio", ratio,
                        "multiplier", surgePricingService.calculateMultiplier(area))));
    }
}
