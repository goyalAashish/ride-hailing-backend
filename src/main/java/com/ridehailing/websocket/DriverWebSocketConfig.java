package com.ridehailing.websocket;

import com.ridehailing.service.DriverService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.socket.config.annotation.EnableWebSocket;
import org.springframework.web.socket.config.annotation.WebSocketConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry;

@Configuration
@EnableWebSocket
public class DriverWebSocketConfig implements WebSocketConfigurer {

    private final DriverService driverService;
    private final ObjectMapper objectMapper;

    public DriverWebSocketConfig(DriverService driverService, ObjectMapper objectMapper) {
        this.driverService = driverService;
        this.objectMapper = objectMapper;
    }

    @Override
    public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {
        registry.addHandler(
                new DriverPresenceWebSocketHandler(driverService, objectMapper),
                "/ws/driver/{driverId}");
    }
}
