package com.github.scheduler.global.config.chat;

import com.corundumstudio.socketio.SocketIOClient;
import com.corundumstudio.socketio.SocketIOServer;
import com.github.scheduler.global.config.auth.JwtTokenProvider;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Component
@AllArgsConstructor
@Slf4j
public class JwtSocketAuthenticator {
    private final JwtTokenProvider jwtTokenProvider;

    public void authenticate(SocketIOClient client) {
        String token = client.getHandshakeData().getSingleUrlParam("token");
        if (token == null || !jwtTokenProvider.validateToken(token)) {
            client.disconnect();
        } else {
            log.info(jwtTokenProvider.getEmailByToken(token));
        }
    }
}
