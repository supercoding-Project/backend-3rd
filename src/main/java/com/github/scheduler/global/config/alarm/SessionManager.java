package com.github.scheduler.global.config.alarm;

import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.WebSocketSession;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Component
public class SessionManager {
    private final ConcurrentHashMap<Long, String> sessions = new ConcurrentHashMap<>();
    private final Set<Long> connectedUserIds = new HashSet<>();

    // 사용자 ID로 세션을 저장
    public void addSession(Long userId, String sessionId) {
        sessions.put(userId, sessionId);  // WebSocketSession 대신 sessionId 저장
        connectedUserIds.add(userId);
        log.info("세션 추가됨: 사용자 ID - {}, 세션 ID - {}", userId, sessionId);
    }

    // 사용자 ID로 세션을 제거
    public void removeSession(Long userId) {
        sessions.remove(userId);
        connectedUserIds.remove(userId);
        log.info("세션 제거됨: 사용자 ID - {}", userId);
    }

    // 사용자 ID로 세션을 조회
    public String getSessionIdByUserId(Long userId) {
        return sessions.get(userId);  // WebSocketSession 대신 sessionId 반환
    }

    // 현재 접속된 모든 사용자 ID 반환
    public Set<Long> getConnectedUsers() {
        log.info("현재 접속 중인 사용자들: {}", connectedUserIds);
        return connectedUserIds;
    }

    // WebSocket 세션에서 userId를 가져오는 메서드
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
