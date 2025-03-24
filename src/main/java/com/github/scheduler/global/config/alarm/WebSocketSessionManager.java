package com.github.scheduler.global.config.alarm;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.WebSocketSession;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Component
public class WebSocketSessionManager {
    // 사용자 ID와 WebSocketSession을 매핑
    private final Map<Long, WebSocketSession> userSessionMap = new ConcurrentHashMap<>();
    // 세션 ID와 사용자 ID를 매핑
    private final Map<String, Long> sessionIdUserMap = new ConcurrentHashMap<>();

    // 사용자의 WebSocket 세션 추가
    public void addSession(Long userId, WebSocketSession session) {
        userSessionMap.put(userId, session);  // 사용자 ID -> 세션
        sessionIdUserMap.put(session.getId(), userId);  // 세션 ID -> 사용자 ID
        System.out.println("세션 추가됨: 사용자 ID - " + userId + ", 세션 ID - " + session.getId());
    }

    // 사용자 ID에 해당하는 세션을 제거
    public void removeSession(Long userId) {
        WebSocketSession session = userSessionMap.remove(userId);  // 세션을 먼저 제거
        if (session != null) {
            sessionIdUserMap.remove(session.getId());  // 세션 ID에서 사용자 ID 제거
        }
        System.out.println("세션 제거됨: 사용자 ID - " + userId);
    }

    // 세션 ID에 해당하는 사용자 ID를 반환
    public Long getUserId(String sessionId) {
        return sessionIdUserMap.get(sessionId);  // 세션 ID -> 사용자 ID 조회
    }

    // 연결된 모든 사용자 ID 반환
    public Set<Long> getConnectedUsers() {
        return userSessionMap.keySet();
    }

    // 특정 사용자 ID에 대한 WebSocketSession 반환
    public WebSocketSession getSession(Long userId) {
        return userSessionMap.get(userId);
    }

    // 세션 존재 여부 확인
    public boolean isSessionActive(Long userId) {
        return userSessionMap.containsKey(userId);
    }
}