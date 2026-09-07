package com.ridehailing.websocket;

import com.ridehailing.service.DriverService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.TextMessage;

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

    @Test
    void locationUpdateMessage_updatesDriverCoordinates() throws Exception {
        handler.handleMessage(session, new TextMessage("""
                {"type":"LOCATION_UPDATE","x":12.5,"y":8.0}
                """));

        verify(driverService).updateLocation(42L, 12.5, 8.0);
    }

    @Test
    void invalidLocationUpdateMessage_isRejected() {
        org.assertj.core.api.Assertions.assertThatThrownBy(() ->
                        handler.handleMessage(session, new TextMessage("""
                                {"type":"LOCATION_UPDATE","x":"bad","y":8.0}
                                """)))
                .isInstanceOf(IllegalArgumentException.class);
    }
}
