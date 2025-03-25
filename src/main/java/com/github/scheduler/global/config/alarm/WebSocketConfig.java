package com.github.scheduler.global.config.alarm;

import com.github.scheduler.global.config.auth.custom.CustomUserDetails;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.web.server.ConfigurableWebServerFactory;
import org.springframework.boot.web.server.WebServerFactoryCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.event.EventListener;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.WebSocketMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;
import org.springframework.web.socket.messaging.SessionConnectEvent;
import org.springframework.web.socket.messaging.SessionDisconnectEvent;

@Slf4j
@Configuration
@EnableScheduling
@EnableWebSocketMessageBroker
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

    @Autowired
    private JwtHandshakeInterceptor jwtHandshakeInterceptor;

    @Autowired
    private SessionManager sessionManager;

    @Override
    public void configureMessageBroker(MessageBrokerRegistry registry) {
        // 클라이언트로부터 /app으로 시작되는 메시지를 처리
        registry.setApplicationDestinationPrefixes("/app");

        // 클라이언트에게 메시지를 전달할 경로 설정
        registry.enableSimpleBroker("/queue", "/topic");
    }

    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        // WebSocket 엔드포인트에 인터셉터 추가
        registry.addEndpoint("/alarms")
                .setAllowedOriginPatterns("*")
                .addInterceptors(jwtHandshakeInterceptor)  // JWT 인터셉터 추가
                .withSockJS();

        registry.addEndpoint("/alarms")
                .setAllowedOriginPatterns("*")
                .addInterceptors(jwtHandshakeInterceptor);  // JWT 인터셉터 추가
    }

    // WebSocket 연결 시 세션을 추가
    @EventListener
    public void handleWebSocketConnectListener(SessionConnectEvent event) {
        log.info("WebSocket 연결 이벤트 수신");
        WebSocketSession session = event.getMessage().getHeaders().get("simpSessionId", WebSocketSession.class);
        Long userId = getUserIdFromSession(session);
        sessionManager.addSession(userId, session);
        log.info("웹소켓 연결됨: 사용자 ID - {}, 세션 ID - {}", userId, session.getId());
    }

    // WebSocket 연결 해제 시 세션을 제거
    @EventListener
    public void handleWebSocketDisconnectListener(SessionDisconnectEvent event) {
        try {
            WebSocketSession session = event.getMessage().getHeaders().get("simpSessionId", WebSocketSession.class);
            Long userId = getUserIdFromSession(session);
            sessionManager.removeSession(userId);
            log.info("웹소켓 연결 해제됨: 사용자 ID - {}, 세션 ID - {}", userId, session.getId());
        } catch (Exception e) {
            // 예외가 발생한 경우, 에러 로그 대신 경고 또는 정보 로그로 출력
            log.warn("웹소켓 연결 해제 처리 중 오류 발생: {}", e.getMessage());
        }
    }

    private Long getUserIdFromSession(WebSocketSession session) {
        CustomUserDetails userDetails = (CustomUserDetails) session.getAttributes().get("userDetails");
        return userDetails != null ? userDetails.getUserEntity().getUserId() : null;  // 사용자 ID 반환
    }
}