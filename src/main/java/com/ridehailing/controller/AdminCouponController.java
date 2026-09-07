package com.ridehailing.controller;

import com.ridehailing.dto.request.CreateCouponRequest;
import com.ridehailing.dto.response.ApiResponse;
import com.ridehailing.dto.response.CouponResponse;
import com.ridehailing.service.CouponService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/admin/coupons")
public class AdminCouponController {

    private final CouponService couponService;

    public AdminCouponController(CouponService couponService) {
        this.couponService = couponService;
    }

    @PostMapping
    public ResponseEntity<ApiResponse<CouponResponse>> create(@Valid @RequestBody CreateCouponRequest request) {
        return ResponseEntity.ok(ApiResponse.success(
                "Coupon created successfully",
                couponService.create(request)));
    }

    @DeleteMapping("/{code}")
    public ResponseEntity<ApiResponse<CouponResponse>> deactivate(@PathVariable String code) {
        return ResponseEntity.ok(ApiResponse.success(
                "Coupon deactivated successfully",
                couponService.deactivate(code)));
    }
}
