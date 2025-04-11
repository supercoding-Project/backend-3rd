package com.github.scheduler.global.config.alarm;

import com.corundumstudio.socketio.SocketIOClient;
import com.corundumstudio.socketio.SocketIOServer;
import com.corundumstudio.socketio.listener.ConnectListener;
import com.github.scheduler.global.config.auth.JwtTokenProvider;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class AlarmSocketInterceptor implements ConnectListener {

    private final JwtTokenProvider jwtTokenProvider;

//    @Qualifier("alarmSocketServer")
//    private final SocketIOServer server;

    @Override
    public void onConnect(SocketIOClient client) {
        // alarm 인증 처리 로직
        String token = client.getHandshakeData().getSingleUrlParam("token");

        if (token == null || !jwtTokenProvider.validateToken(token)) {
            client.disconnect();
            return;
        }

        // JWT 토큰에서 Authentication 객체 생성
        Authentication authentication = jwtTokenProvider.getAuthentication(token);

        // SecurityContext 설정
        SecurityContext securityContext = SecurityContextHolder.createEmptyContext();
        securityContext.setAuthentication(authentication);
        SecurityContextHolder.setContext(securityContext);
        log.info("[알림 소켓] 인증된 사용자: {}", authentication.getName());

        String email = jwtTokenProvider.getEmailByToken(token);
        client.set("email", email);
    }
}
