package com.github.scheduler.global.config.alarm;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Getter
@Setter
@Configuration
@ConfigurationProperties(prefix = "socket.alarm")
public class AlarmSocketProperties {
    private String host;
    private Integer port;
}
