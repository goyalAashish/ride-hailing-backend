package com.ridehailing.websocket;

import com.ridehailing.service.DriverService;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.socket.config.annotation.EnableWebSocket;
import org.springframework.web.socket.config.annotation.WebSocketConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry;

@Configuration
@EnableWebSocket
public class DriverWebSocketConfig implements WebSocketConfigurer {

    private final DriverService driverService;

    public DriverWebSocketConfig(DriverService driverService) {
        this.driverService = driverService;
    }

    @Override
    public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {
        registry.addHandler(new DriverPresenceWebSocketHandler(driverService), "/ws/driver/{driverId}");
    }
}
