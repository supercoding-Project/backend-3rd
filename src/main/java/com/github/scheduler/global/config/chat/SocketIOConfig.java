package com.github.scheduler.global.config.chat;

import com.corundumstudio.socketio.Configuration;
import com.corundumstudio.socketio.SocketIOServer;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.scheduler.global.config.alarm.AlarmSocketProperties;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.SpringBootConfiguration;
import org.springframework.context.annotation.Bean;

@SpringBootConfiguration
@RequiredArgsConstructor
public class SocketIOConfig {

//    private final SocketProperties socketProperties;

    //private final ObjectMapper objectMapper;

    @Bean(name = "socketIOServer")
    public SocketIOServer socketIOServer(SocketProperties socketProperties) {
        Configuration config = new Configuration();
        config.setHostname(socketProperties.getHost());
        config.setPort(socketProperties.getPort());
        config.setOrigin("*");

        config.setAuthorizationListener(data -> true);

        return new SocketIOServer(config);
    }

    @Bean(name = "alarmSocketServer")
    public SocketIOServer alarmSocketServer(AlarmSocketProperties alarmSocketProperties) {
        Configuration config = new Configuration();
        config.setHostname(alarmSocketProperties.getHost());
        config.setPort(9093); // 기존 채팅 포트와 다른 포트로 설정
        config.setOrigin("*");

        config.setAuthorizationListener(data -> true);

        return new SocketIOServer(config);
    }
}
