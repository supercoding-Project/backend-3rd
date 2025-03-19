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

    public JwtHandshakeInterceptor(JwtTokenProvider jwtTokenProvider,
                                   CustomUserDetailsServiceImpl userDetailsService) {
        this.jwtTokenProvider = jwtTokenProvider;
        this.userDetailsService = userDetailsService;
    }

    @Override
    public boolean beforeHandshake(ServerHttpRequest request, ServerHttpResponse response,
                                   WebSocketHandler wsHandler, Map<String, Object> attributes) {
        if (request instanceof ServletServerHttpRequest) {
            HttpServletRequest httpRequest = ((ServletServerHttpRequest) request).getServletRequest();
            String token = httpRequest.getHeader("Authorization");

            if (token != null && token.startsWith("Bearer ")) {
                token = token.substring(7);
                if (jwtTokenProvider.validateToken(token)) {
                    String email = jwtTokenProvider.getEmailByToken(token);
                    CustomUserDetails userDetails = (CustomUserDetails) userDetailsService.loadUserByUsername(email);

                    attributes.put("userDetails", userDetails);
                    log.info("웹소켓 핸드셰이크 성공: 사용자 이메일 - {}", email);
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