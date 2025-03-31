package com.github.scheduler.alarm.event;

import com.github.scheduler.auth.entity.UserEntity;
import com.github.scheduler.auth.repository.UserRepository;
import com.github.scheduler.global.config.alarm.SessionManager;
import com.github.scheduler.global.config.auth.JwtTokenProvider;
import com.github.scheduler.global.config.auth.custom.CustomUserDetails;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.EventListener;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.messaging.SessionConnectedEvent;
import org.springframework.web.socket.messaging.SessionDisconnectEvent;

@Slf4j
@Component
public class WebSocketEventListener {
    @Autowired
    private final SessionManager sessionManager;
    @Autowired
    private final JwtTokenProvider jwtTokenProvider;
    private final UserRepository userRepository;

    public WebSocketEventListener(SessionManager sessionManager, JwtTokenProvider jwtTokenProvider, UserRepository userRepository) {
        this.sessionManager = sessionManager;
        this.jwtTokenProvider = jwtTokenProvider;
        this.userRepository = userRepository;
    }

    @EventListener
    public void handleWebSocketConnectListener(SessionConnectedEvent event) {
        StompHeaderAccessor headerAccessor = StompHeaderAccessor.wrap(event.getMessage());

        // ✅ WebSocket 헤더에서 토큰 가져오기
        String token = headerAccessor.getFirstNativeHeader("Authorization");
        if (token == null || !token.startsWith("Bearer ")) {
            log.warn("❌ WebSocket 연결 요청 실패: Authorization 헤더 없음");
            return;
        }
        token = token.substring(7);

        // ✅ 토큰 검증
        if (!jwtTokenProvider.validateToken(token)) {
            log.warn("❌ WebSocket 연결 요청 실패: 유효하지 않은 토큰");
            return;
        }

        try {
            // ✅ 토큰에서 이메일 추출 후 DB 조회
            String userEmail = jwtTokenProvider.getEmailByToken(token);
            UserEntity userEntity = userRepository.findByEmail(userEmail)
                    .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다. 이메일: " + userEmail));

            Long userId = userEntity.getUserId();
            String sessionId = headerAccessor.getSessionId();

            // ✅ 세션에 사용자 추가
            sessionManager.addSession(userId, sessionId);
            if (headerAccessor.getSessionAttributes() != null) {
                headerAccessor.getSessionAttributes().put("userId", userId);
            }

            log.info("✅ WebSocket 연결됨: 사용자 ID - {}, 세션 ID - {}", userId, sessionId);
        } catch (Exception e) {
            log.error("❌ WebSocket 연결 중 오류 발생: {}", e.getMessage(), e);
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
}
