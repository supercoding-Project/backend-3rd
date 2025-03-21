package com.github.scheduler.global.config.chat;

import com.corundumstudio.socketio.Configuration;
import com.corundumstudio.socketio.SocketIOServer;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.SpringBootConfiguration;
import org.springframework.context.annotation.Bean;

@SpringBootConfiguration
//@Configuration
@RequiredArgsConstructor
public class SocketIOConfig {

    private final SocketProperties socketProperties;

    @Bean
    public SocketIOServer socketIOServer(SocketSecurityInterceptor socketSecurityInterceptor) {
        Configuration config = new Configuration();
        config.setHostname(socketProperties.getHost());
        config.setPort(socketProperties.getPort());
        config.setAuthorizationListener(data -> true);
        config.setOrigin("*");

        SocketIOServer server = new SocketIOServer(config);
        server.addConnectListener(socketSecurityInterceptor);
        return server;
    }
}
