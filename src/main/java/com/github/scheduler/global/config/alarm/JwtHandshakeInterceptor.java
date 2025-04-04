package com.github.scheduler.global.config.alarm;

import com.github.scheduler.global.config.auth.JwtTokenProvider;
import com.github.scheduler.global.config.auth.custom.CustomUserDetails;
import com.github.scheduler.global.config.auth.custom.CustomUserDetailsServiceImpl;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.http.server.ServletServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.server.HandshakeInterceptor;

import java.util.Map;

@Slf4j
@Component
public class JwtHandshakeInterceptor implements HandshakeInterceptor {
    private final JwtTokenProvider jwtTokenProvider;
    private final CustomUserDetailsServiceImpl userDetailsService;
    private final WebSocketSessionManager sessionManager;

    public JwtHandshakeInterceptor(JwtTokenProvider jwtTokenProvider,
                                   CustomUserDetailsServiceImpl userDetailsService,
                                   WebSocketSessionManager sessionManager) {
        this.jwtTokenProvider = jwtTokenProvider;
        this.userDetailsService = userDetailsService;
        this.sessionManager = sessionManager;
    }

    @Override
    public boolean beforeHandshake(ServerHttpRequest request, ServerHttpResponse response,
                                   WebSocketHandler wsHandler, Map<String, Object> attributes) {
        if (request instanceof ServletServerHttpRequest) {
            HttpServletRequest httpRequest = ((ServletServerHttpRequest) request).getServletRequest();

            // Authorization 헤더에서 Bearer 토큰을 추출
            String authorizationHeader = httpRequest.getHeader("Authorization");
            log.info("Authorization header: {}", authorizationHeader);

            // Authorization 헤더에서 "Bearer " 부분을 제외한 토큰을 추출
            if (authorizationHeader != null && authorizationHeader.startsWith("Bearer ")) {
                String token = authorizationHeader.substring(7);  // "Bearer "를 제외한 부분만 추출
                log.info("Received token: {}", token);

                // 토큰이 유효한지 검사
                if (jwtTokenProvider.validateToken(token)) {
                    String email = jwtTokenProvider.getEmailByToken(token);
                    CustomUserDetails userDetails = (CustomUserDetails) userDetailsService.loadUserByUsername(email);

                    // WebSocket 세션에 사용자 정보 저장
                    attributes.put("userDetails", userDetails);
                    log.info("웹소켓 핸드셰이크 성공: 사용자 이메일 - {}", email);

                    // 세션 정보를 sessionManager에 저장
                    sessionManager.addSession(userDetails.getUserEntity().getUserId());

                    return true;
                }
            }
        }

        log.warn("웹소켓 핸드셰이크 실패: 유효한 JWT 토큰이 없음");
        return false;
    }

    @Override
    public void afterHandshake(ServerHttpRequest request, ServerHttpResponse response,
                               WebSocketHandler wsHandler, Exception exception) {
        // 핸드셰이크 이후 특별한 처리가 필요하면 여기에 추가
    }
}