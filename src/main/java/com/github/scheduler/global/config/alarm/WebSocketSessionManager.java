package com.github.scheduler.global.config.alarm;

import com.corundumstudio.socketio.SocketIOClient;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.WebSocketSession;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

@Slf4j
@Component
public class WebSocketSessionManager {
    private final Map<Long, WebSocketSession> userSessions = new HashMap<>();
    private final Map<String, Long> sessionUserMap = new HashMap<>();  // String으로 세션 ID 관리

    // 사용자 세션 추가
    public void addSession(Long userId, WebSocketSession session) {
        userSessions.put(userId, session);
        sessionUserMap.put(session.getId(), userId);  // 세션 ID를 사용하여 매핑
        log.info("세션 추가: 사용자 ID - {}", userId);
    }

    // 사용자 세션 제거
    public void removeSession(Long userId) {
        WebSocketSession removedSession = userSessions.remove(userId);
        if (removedSession != null) {
            sessionUserMap.remove(removedSession.getId());  // 세션 ID로 제거
            log.info("세션 제거: 사용자 ID - {}", userId);
        } else {
            log.warn("세션을 찾을 수 없습니다: 사용자 ID - {}", userId);
        }
    }

    // 사용자의 세션 조회
    public WebSocketSession getSession(Long userId) {
        return userSessions.get(userId);
    }

    // 연결된 사용자 목록 가져오기
    public Set<Long> getConnectedUsers() {
        return userSessions.keySet();
    }

    // SocketIOClient에서 세션 ID를 가져와 사용자 ID 조회
    public Long getUserId(SocketIOClient client) {
        String sessionId = client.getSessionId().toString();  // 세션 ID는 String 타입
        return sessionUserMap.get(sessionId);  // 세션 ID로 사용자 ID 조회
    }
}
