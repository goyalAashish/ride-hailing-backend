package com.ridehailing.controller;

import com.ridehailing.dto.response.RideResponse;
import com.ridehailing.exception.GlobalExceptionHandler;
import com.ridehailing.service.RideService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(RideController.class)
@Import(GlobalExceptionHandler.class)
class RideControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private RideService rideService;

    @Test
    void request_withoutUserHeader_returnsHttp400() throws Exception {
        mockMvc.perform(post("/api/v1/rides/request")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "pickupX":0,
                                  "pickupY":0,
                                  "destX":1,
                                  "destY":1,
                                  "carType":"SEDAN"
                                }
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error.code").value("MISSING_HEADER"));
    }
}
