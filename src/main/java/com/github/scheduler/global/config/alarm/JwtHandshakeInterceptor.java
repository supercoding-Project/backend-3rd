package com.github.scheduler.global.config.alarm;

import com.github.scheduler.global.config.auth.JwtTokenProvider;
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

    public JwtHandshakeInterceptor(JwtTokenProvider jwtTokenProvider,
                                   CustomUserDetailsServiceImpl userDetailsService,
                                   SessionManager sessionManager) {
        this.jwtTokenProvider = jwtTokenProvider;
    }

    @Override
    public boolean beforeHandshake(ServerHttpRequest request, ServerHttpResponse response,
                                   WebSocketHandler wsHandler, Map<String, Object> attributes) throws Exception{
        String token = null;

        if (request instanceof ServletServerHttpRequest) {
            HttpServletRequest httpRequest = ((ServletServerHttpRequest) request).getServletRequest();
            token = httpRequest.getParameter("token");
        }
        if (token != null && jwtTokenProvider.validateToken(token)) {
            String email = jwtTokenProvider.getEmailByToken(token);
            StompPrincipal principal = new StompPrincipal(email);
            attributes.put("email", email);
            attributes.put("simpUser", principal);

            if (request instanceof ServletServerHttpRequest) {
                ((ServletServerHttpRequest) request).getServletRequest().setAttribute("simpUser", principal);
            }
            return true;
        }

        return false;
    }


//    @Override
//    public boolean beforeHandshake(@NonNull ServerHttpRequest request,@NonNull ServerHttpResponse response,
//                                   @NonNull WebSocketHandler wsHandler, @NonNull Map<String, Object> attributes) {
//
//        if (request instanceof ServletServerHttpRequest) {
//            HttpServletRequest httpRequest = ((ServletServerHttpRequest) request).getServletRequest();
//            // Authorization 헤더에서 Bearer 토큰을 추출
//            String authorizationHeader = httpRequest.getHeader("Authorization");
//            log.info("Authorization header: {}", authorizationHeader);
//
//            // Authorization 헤더에서 "Bearer " 부분을 제외한 토큰을 추출
//            if (authorizationHeader != null && authorizationHeader.startsWith("Bearer ")) {
//                String token = authorizationHeader.substring(7);  // "Bearer "를 제외한 부분만 추출
//                log.info("Received token: {}", token);
//
//                // 토큰이 유효한지 검사
//                if (jwtTokenProvider.validateToken(token)) {
//                    String email = jwtTokenProvider.getEmailByToken(token);
//                    CustomUserDetails userDetails = (CustomUserDetails) userDetailsService.loadUserByUsername(email);
//
//                    // WebSocket 세션에 사용자 정보 저장
//                    attributes.put("userDetails", userDetails);
//                    log.info("웹소켓 핸드셰이크 성공: 사용자 이메일 - {}", email);
//
//                    sessionManager.addSession(userDetails.getUserEntity().getUserId(), attributes.get("userDetails").toString());
//                    log.info("세션 추가됨: 사용자 ID - {}", userDetails.getUserEntity().getUserId());
//
//                    return true;
//                } else {
//                log.warn("유효하지 않은 JWT 토큰입니다.");
//                }
//            }
//        }
//
//        log.warn("웹소켓 핸드셰이크 실패: 유효한 JWT 토큰이 없음");
//        return false;
//    }


    @Override
    public void afterHandshake(ServerHttpRequest request, ServerHttpResponse response,
                               WebSocketHandler wsHandler, Exception exception) {
        // 핸드셰이크 이후 특별한 처리가 필요하면 여기에 추가
    }
}