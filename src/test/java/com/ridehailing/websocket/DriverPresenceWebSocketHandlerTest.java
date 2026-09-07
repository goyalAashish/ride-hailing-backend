package com.ridehailing.websocket;

import com.ridehailing.service.DriverService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.WebSocketSession;

import java.net.URI;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class DriverPresenceWebSocketHandlerTest {

    private DriverService driverService;
    private DriverPresenceWebSocketHandler handler;
    private WebSocketSession session;

    @BeforeEach
    void setUp() {
        driverService = mock(DriverService.class);
        handler = new DriverPresenceWebSocketHandler(driverService);
        session = mock(WebSocketSession.class);
        when(session.getUri()).thenReturn(URI.create("ws://localhost/ws/driver/42"));
    }

    @Test
    void connection_marksDriverAvailable() {
        handler.afterConnectionEstablished(session);

        verify(driverService).markAvailable(42L);
    }

    @Test
    void disconnection_marksDriverOffline() {
        handler.afterConnectionClosed(session, CloseStatus.NORMAL);

        verify(driverService).markOffline(42L);
    }

    @Test
    void invalidPath_isRejected() {
        when(session.getUri()).thenReturn(URI.create("ws://localhost/ws/driver/not-a-number"));

        org.assertj.core.api.Assertions.assertThatThrownBy(() -> handler.afterConnectionEstablished(session))
                .isInstanceOf(IllegalArgumentException.class);
    }
}
