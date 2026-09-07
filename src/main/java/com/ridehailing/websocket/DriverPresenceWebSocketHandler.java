package com.ridehailing.websocket;

import com.ridehailing.service.DriverService;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.net.URI;

/**
 * Tracks driver presence using the WebSocket connection lifecycle.
 */
public class DriverPresenceWebSocketHandler extends TextWebSocketHandler {

    private static final String DRIVER_PATH_PREFIX = "/ws/driver/";

    private final DriverService driverService;

    public DriverPresenceWebSocketHandler(DriverService driverService) {
        this.driverService = driverService;
    }

    @Override
    public void afterConnectionEstablished(WebSocketSession session) {
        driverService.markAvailable(driverIdFrom(session));
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) {
        driverService.markOffline(driverIdFrom(session));
    }

    private Long driverIdFrom(WebSocketSession session) {
        URI uri = session.getUri();
        if (uri == null || uri.getPath() == null || !uri.getPath().startsWith(DRIVER_PATH_PREFIX)) {
            throw new IllegalArgumentException("WebSocket path must be /ws/driver/{driverId}");
        }

        String driverId = uri.getPath().substring(DRIVER_PATH_PREFIX.length());
        if (driverId.isBlank() || driverId.contains("/")) {
            throw new IllegalArgumentException("WebSocket path must include a valid driver id");
        }

        try {
            return Long.valueOf(driverId);
        } catch (NumberFormatException exception) {
            throw new IllegalArgumentException("WebSocket path must include a numeric driver id", exception);
        }
    }
}
