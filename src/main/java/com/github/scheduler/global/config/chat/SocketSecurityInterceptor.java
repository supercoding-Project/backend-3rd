package com.github.scheduler.global.config.chat;

import com.corundumstudio.socketio.AckRequest;
import com.corundumstudio.socketio.SocketIOClient;
import com.corundumstudio.socketio.SocketIOServer;
import com.corundumstudio.socketio.listener.ConnectListener;
import com.corundumstudio.socketio.listener.DataListener;
import com.corundumstudio.socketio.protocol.Event;
import com.github.scheduler.chat.dto.ChatRoomCreate;
import com.github.scheduler.global.config.auth.JwtTokenProvider;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class SocketSecurityInterceptor implements ConnectListener{

    private final JwtTokenProvider jwtTokenProvider;
    private final SocketIOServer server;

    @Override
    public void onConnect(SocketIOClient client) {
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

        log.info("Client authenticated: {}", authentication.getName());
    }


}
