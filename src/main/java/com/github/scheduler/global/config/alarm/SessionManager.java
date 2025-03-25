package com.github.scheduler.global.config.alarm;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.WebSocketSession;

import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Component
public class SessionManager {
    private final ConcurrentHashMap<Long, WebSocketSession> sessions = new ConcurrentHashMap<>();

    // 사용자 ID로 세션을 저장
    public void addSession(Long userId, WebSocketSession session) {
        sessions.put(userId, session);
        log.info("세션 추가됨: 사용자 ID - {}, 세션 ID - {}", userId, session.getId());
    }

    // 사용자 ID로 세션을 제거
    public void removeSession(Long userId) {
        sessions.remove(userId);
        log.info("세션 제거됨: 사용자 ID - {}", userId);
    }

    // 사용자 ID로 세션을 조회
    public WebSocketSession getSessionByUserId(Long userId) {
        return sessions.get(userId);
    }

    // 현재 접속된 모든 사용자 ID를 반환
    public Set<Long> getConnectedUsers() {
        log.info("현재 접속 중인 사용자들: {}", sessions.keySet());
        return sessions.keySet();
    }
}