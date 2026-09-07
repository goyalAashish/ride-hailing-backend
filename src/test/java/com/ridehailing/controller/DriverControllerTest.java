package com.ridehailing.controller;

import com.ridehailing.domain.enums.CarType;
import com.ridehailing.domain.enums.DriverStatus;
import com.ridehailing.dto.response.DriverResponse;
import com.ridehailing.dto.request.UpdateLocationRequest;
import com.ridehailing.dto.response.PageResponse;
import com.ridehailing.dto.response.RideResponse;
import com.ridehailing.exception.DuplicateResourceException;
import com.ridehailing.exception.GlobalExceptionHandler;
import com.ridehailing.service.DriverService;
import com.ridehailing.service.RideHistoryService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(DriverController.class)
@Import(GlobalExceptionHandler.class)
class DriverControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private DriverService driverService;

    @MockBean
    private RideHistoryService rideHistoryService;

    @Test
    void register_returnsHttp200Envelope() throws Exception {
        when(driverService.register(any())).thenReturn(
                new DriverResponse(1L, "Ravi", "888", "KA-01-AA-1111", CarType.SEDAN, DriverStatus.OFFLINE));

        mockMvc.perform(post("/api/v1/drivers")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "name":"Ravi",
                                  "phone":"888",
                                  "registrationNumber":"KA-01-AA-1111",
                                  "carType":"SEDAN"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.statusCode").value(200))
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.driverId").value(1))
                .andExpect(jsonPath("$.data.status").value("OFFLINE"))
                .andExpect(jsonPath("$.data.carType").value("SEDAN"));
    }

    @Test
    void register_missingCarType_returnsHttp400() throws Exception {
        mockMvc.perform(post("/api/v1/drivers")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"name":"Ravi","phone":"888","registrationNumber":"KA-1"}
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error.code").value("VALIDATION_ERROR"));
    }

    @Test
    void register_duplicateRegistration_returnsHttp400() throws Exception {
        when(driverService.register(any()))
                .thenThrow(new DuplicateResourceException("Driver", "registrationNumber", "KA-1"));

        mockMvc.perform(post("/api/v1/drivers")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "name":"Ravi",
                                  "phone":"888",
                                  "registrationNumber":"KA-1",
                                  "carType":"HATCHBACK"
                                }
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error.code").value("DUPLICATE_RESOURCE"));
    }

    @Test
    void history_returnsPaginatedEnvelope() throws Exception {
        when(rideHistoryService.driverHistory(1L, 0, 10))
                .thenReturn(new PageResponse<>(java.util.List.<RideResponse>of(), 0, 10, 0, 0, true, true));

        mockMvc.perform(get("/api/v1/drivers/rides/history")
                        .header("X-Driver-Id", "1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.statusCode").value(200))
                .andExpect(jsonPath("$.data.page").value(0))
                .andExpect(jsonPath("$.data.size").value(10))
                .andExpect(jsonPath("$.data.content").isEmpty());
    }

    @Test
    void updateLocation_returnsUpdatedDriver() throws Exception {
        when(driverService.updateLocation(1L, 12.5, 8.0))
                .thenReturn(new DriverResponse(1L, "Ravi", "888", "KA-01-AA-1111",
                        CarType.SEDAN, DriverStatus.AVAILABLE));

        mockMvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders
                        .patch("/api/v1/drivers/1/location")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"x":12.5,"y":8.0}
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.driverId").value(1))
                .andExpect(jsonPath("$.data.status").value("AVAILABLE"));

        verify(driverService).updateLocation(1L, 12.5, 8.0);
    }
}
