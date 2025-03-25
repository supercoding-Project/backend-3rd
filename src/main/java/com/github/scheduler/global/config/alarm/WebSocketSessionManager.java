package com.github.scheduler.global.config.alarm;

import com.corundumstudio.socketio.SocketIOClient;
import com.github.scheduler.alarm.dto.SchedulerAlarmDto;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.stereotype.Component;

import java.util.HashSet;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.Set;
import java.util.stream.Collectors;

@Slf4j
@Component
public class WebSocketSessionManager {
    private final Set<Long> connectedUserIds = new HashSet<>();

    public void addSession(Long userId) {
        connectedUserIds.add(userId);
    }

    public void removeSession(Long userId) {
        connectedUserIds.remove(userId);
    }

    public Set<Long> getConnectedUsers() {
        return new HashSet<>(connectedUserIds);
    }

    public Long getUserId(SimpMessageHeaderAccessor headerAccessor) {
        if (headerAccessor.getSessionAttributes() != null) {
            Object userId = headerAccessor.getSessionAttributes().get("userId");
            if (userId instanceof Long) {
                return (Long) userId;
            }
        }
        return null;
    }
}
