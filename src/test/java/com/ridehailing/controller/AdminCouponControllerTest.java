package com.ridehailing.controller;

import com.ridehailing.dto.response.CouponResponse;
import com.ridehailing.exception.GlobalExceptionHandler;
import com.ridehailing.service.CouponService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(AdminCouponController.class)
@Import(GlobalExceptionHandler.class)
class AdminCouponControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private CouponService couponService;

    @Test
    void create_returnsHttp200Envelope() throws Exception {
        when(couponService.create(any())).thenReturn(
                new CouponResponse("SAVE10", new BigDecimal("10"), new BigDecimal("50"), 1, true));

        mockMvc.perform(post("/api/v1/admin/coupons")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "code":"save10",
                                  "discountPercentage":10,
                                  "maxDiscountAmount":50,
                                  "maxUsagePerUser":1
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.statusCode").value(200))
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.code").value("SAVE10"))
                .andExpect(jsonPath("$.data.active").value(true));
    }

    @Test
    void create_missingField_returnsHttp400() throws Exception {
        mockMvc.perform(post("/api/v1/admin/coupons")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"code":"SAVE10","discountPercentage":10}
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error.code").value("VALIDATION_ERROR"));
    }

    @Test
    void deactivate_returnsHttp200Envelope() throws Exception {
        when(couponService.deactivate("SAVE10"))
                .thenReturn(new CouponResponse("SAVE10", new BigDecimal("10"), new BigDecimal("50"), 1, false));

        mockMvc.perform(delete("/api/v1/admin/coupons/SAVE10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.code").value("SAVE10"))
                .andExpect(jsonPath("$.data.active").value(false));
    }
}
