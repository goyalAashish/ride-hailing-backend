package com.ridehailing.controller;

import com.ridehailing.dto.response.UserResponse;
import com.ridehailing.dto.response.PageResponse;
import com.ridehailing.dto.response.RideResponse;
import com.ridehailing.exception.DuplicateResourceException;
import com.ridehailing.exception.GlobalExceptionHandler;
import com.ridehailing.service.UserService;
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
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(UserController.class)
@Import(GlobalExceptionHandler.class)
class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private UserService userService;

    @MockBean
    private RideHistoryService rideHistoryService;

    @Test
    void register_returnsHttp200Envelope() throws Exception {
        when(userService.register(any())).thenReturn(new UserResponse(1L, "Ada", "999"));

        mockMvc.perform(post("/api/v1/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"name":"Ada","phone":"999"}
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.statusCode").value(200))
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("User registered successfully"))
                .andExpect(jsonPath("$.data.userId").value(1))
                .andExpect(jsonPath("$.data.phone").value("999"))
                .andExpect(jsonPath("$.error").isEmpty());
    }

    @Test
    void register_blankFields_returnsHttp400() throws Exception {
        mockMvc.perform(post("/api/v1/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"name":"","phone":" "}
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.error.code").value("VALIDATION_ERROR"));
    }

    @Test
    void register_duplicatePhone_returnsHttp400() throws Exception {
        when(userService.register(any())).thenThrow(new DuplicateResourceException("User", "phone", "999"));

        mockMvc.perform(post("/api/v1/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"name":"Ada","phone":"999"}
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error.code").value("DUPLICATE_RESOURCE"));
    }

    @Test
    void history_returnsPaginatedEnvelope() throws Exception {
        when(rideHistoryService.userHistory(1L, 0, 10))
                .thenReturn(new PageResponse<>(java.util.List.<RideResponse>of(), 0, 10, 0, 0, true, true));

        mockMvc.perform(get("/api/v1/users/rides/history")
                        .header("X-User-Id", "1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.statusCode").value(200))
                .andExpect(jsonPath("$.data.page").value(0))
                .andExpect(jsonPath("$.data.size").value(10))
                .andExpect(jsonPath("$.data.content").isEmpty());
    }
}
