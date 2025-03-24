package com.github.scheduler.alarm.event;

import com.github.scheduler.global.config.alarm.WebSocketSessionManager;
import com.github.scheduler.global.config.auth.JwtTokenProvider;
import com.github.scheduler.global.config.auth.custom.CustomUserDetails;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.messaging.SessionConnectedEvent;
import org.springframework.web.socket.messaging.SessionDisconnectEvent;

import java.util.HashMap;
import java.util.Map;

@Slf4j
@Component
public class WebSocketEventListener {
    private final WebSocketSessionManager sessionManager;
    private final JwtTokenProvider jwtTokenProvider;
    private final UserDetailsService userDetailsService;

    public WebSocketEventListener(WebSocketSessionManager sessionManager, JwtTokenProvider jwtTokenProvider, UserDetailsService userDetailsService) {
        this.sessionManager = sessionManager;
        this.jwtTokenProvider = jwtTokenProvider;
        this.userDetailsService = userDetailsService;
    }

    @EventListener
    public void handleWebSocketConnectListener(SessionConnectedEvent event) {
        StompHeaderAccessor headerAccessor = StompHeaderAccessor.wrap(event.getMessage());

        if (headerAccessor.getSessionAttributes() == null) {
            headerAccessor.setSessionAttributes(new HashMap<>());
        }

        CustomUserDetails userDetails = (CustomUserDetails) headerAccessor.getSessionAttributes().get("userDetails");

        if (userDetails != null) {
            Long userId = userDetails.getUserEntity().getUserId();
            WebSocketSession session = (WebSocketSession) event.getMessage().getHeaders().get("simpSessionId");

            sessionManager.addSession(userId, session);
            log.info("✅ WebSocket 연결됨: 사용자 ID - {}", userId);
        } else {
            log.warn("❌ 사용자 정보가 없습니다.");
        }
    }

    @EventListener
    public void handleWebSocketDisconnectListener(SessionDisconnectEvent event) {
        StompHeaderAccessor headerAccessor = StompHeaderAccessor.wrap(event.getMessage());
        CustomUserDetails userDetails = (CustomUserDetails) headerAccessor.getSessionAttributes().get("userDetails");

        if (userDetails != null) {
            Long userId = userDetails.getUserEntity().getUserId();
            sessionManager.removeSession(userId);
            log.info("❌ WebSocket 연결 종료: 사용자 ID - {}", userId);
        }
    }
}
