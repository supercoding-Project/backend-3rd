package com.github.scheduler.global.config.chat;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Getter
@Setter
@Configuration
@ConfigurationProperties(prefix = "socket.server")
public class SocketProperties {
    private String host;
    private Integer port;
}
