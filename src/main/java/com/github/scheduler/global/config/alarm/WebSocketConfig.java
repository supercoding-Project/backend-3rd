package com.github.scheduler.global.config.alarm;

import com.github.scheduler.auth.repository.UserRepository;
import com.github.scheduler.global.config.auth.JwtTokenProvider;
import com.github.scheduler.global.config.auth.custom.CustomUserDetails;
import com.github.scheduler.global.config.auth.custom.CustomUserDetailsServiceImpl;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.event.EventListener;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageHeaders;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;
import org.springframework.web.socket.messaging.SessionConnectedEvent;
import org.springframework.web.socket.messaging.SessionDisconnectEvent;
import org.springframework.web.socket.server.support.DefaultHandshakeHandler;

import java.security.Principal;
import java.util.Map;

@Slf4j
@Configuration
@EnableScheduling
@EnableWebSocketMessageBroker
@RequiredArgsConstructor
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

    @Autowired
    private final SessionManager sessionManager;

    @Autowired
    private final JwtTokenProvider jwtTokenProvider;
    private final UserRepository userRepository;
    private final JwtHandshakeInterceptor jwtHandshakeInterceptor;
    private final CustomUserDetailsServiceImpl userDetailsService;

    @Override
    public void configureMessageBroker(MessageBrokerRegistry registry) {
        // 클라이언트로부터 /app으로 시작되는 메시지를 처리
        registry.setApplicationDestinationPrefixes("/app");

        registry.setUserDestinationPrefix("/user");

        // 클라이언트에게 메시지를 전달할 경로 설정
        registry.enableSimpleBroker("/topic", "/queue");
    }

    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        registry.addEndpoint("/ws/*")
                .addInterceptors(jwtHandshakeInterceptor)
                .setHandshakeHandler(new DefaultHandshakeHandler() {
                    @Override
                    protected Principal determineUser(ServerHttpRequest request, WebSocketHandler wsHandler, Map<String, Object> attributes) {
                        return (Principal) attributes.get("simpUser");
                    }
                })
                .setAllowedOriginPatterns("http://*", "https://*")
                .withSockJS();  // JWT 인증을 위한 인터셉터 추가
    }

    @EventListener
    public void handleWebSocketConnectListener(SessionConnectedEvent event) {
        StompHeaderAccessor headerAccessor = StompHeaderAccessor.wrap(event.getMessage());
        Message<?> simpConnectMessage = (Message<?>) headerAccessor.getHeader("simpConnectMessage");

        Principal user = headerAccessor.getUser();

        if (user != null) {
            log.info("✅ 현재 STOMP 세션에서 인식된 사용자: {}", user.getName());
        } else {
            log.warn("⚠ STOMP 세션에서 사용자 정보를 찾을 수 없음!");
        }
        if (simpConnectMessage == null) {
           return;
        }

        MessageHeaders headers = simpConnectMessage.getHeaders();
        Map<String, Object> sessionAttributes = (Map<String, Object>) headers.get("simpSessionAttributes");
        if (sessionAttributes != null && sessionAttributes.containsKey("email")) {

            CustomUserDetails userDetails = (CustomUserDetails) userDetailsService.loadUserByUsername(sessionAttributes.get("email").toString());

        if (userDetails == null) {
            log.warn("❌ WebSocket 연결 요청 실패: userDetails를 찾을 수 없음");
            return;
        }
        Long userId = userDetails.getUserEntity().getUserId();
        String sessionId = headerAccessor.getSessionId();
        sessionManager.addSession(userId, sessionId);

        log.info("✅ WebSocket 연결됨: 사용자 ID - {}, 세션 ID - {}", userId, sessionId);
        }
    }


    @EventListener
    public void handleWebSocketDisconnectListener(SessionDisconnectEvent event) {
        StompHeaderAccessor headerAccessor = StompHeaderAccessor.wrap(event.getMessage());
        Long userId = sessionManager.getUserId(headerAccessor);

        if (userId != null) {
            sessionManager.removeSession(userId);
            log.info("❌ WebSocket 연결 종료: 사용자 ID - {}", userId);
        } else {
            log.warn("❌ WebSocket 연결 종료: 사용자 ID 찾을 수 없음");
        }
    }

    private Long getUserIdFromSession(WebSocketSession session) {
        CustomUserDetails userDetails = (CustomUserDetails) session.getAttributes().get("userDetails");
        return userDetails != null ? userDetails.getUserEntity().getUserId() : null;  // 사용자 ID 반환
    }
}