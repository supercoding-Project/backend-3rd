package com.github.scheduler.chat.handler;

import com.corundumstudio.socketio.SocketIOClient;
import com.corundumstudio.socketio.SocketIOServer;
import com.corundumstudio.socketio.annotation.OnConnect;
import com.corundumstudio.socketio.annotation.OnDisconnect;
import com.corundumstudio.socketio.annotation.OnEvent;
import com.github.scheduler.chat.dto.ChatRoomCreate;
import com.github.scheduler.chat.dto.ChatRoomDto;
import com.github.scheduler.chat.entity.ChatRoom;
import com.github.scheduler.chat.service.ChatService;
import com.github.scheduler.global.config.auth.custom.CustomUserDetails;
import com.github.scheduler.global.config.chat.JwtSocketAuthenticator;
import com.github.scheduler.global.dto.ApiResponse;
import com.github.scheduler.global.exception.ErrorCode;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Component;

@Tag(name = "Socket.io Chat", description = "Socket.io 기반 채팅 API")
@Component
@Slf4j
@RequiredArgsConstructor
public class ChatEventHandler {
    private final SocketIOServer server;
    private final ChatService chatService;
    private final JwtSocketAuthenticator jwtSocketAuthenticator;

    @PostConstruct
    public void startServer() {
        server.start();
        Runtime.getRuntime().addShutdownHook(new Thread(server::stop));
    }
    @PreDestroy
    public void stopServer() {
        server.stop();
    }
    // 클라이언트 연결 시 실행
    @OnConnect
    public void onConnect(SocketIOClient client) {
        //JWT 토큰 인증
        jwtSocketAuthenticator.authenticate(client);
        log.info("Client connected: {}", client.getSessionId());
    }
    // 클라이언트 연결 종료 시 실행
    @OnDisconnect
    public void onDisconnect(SocketIOClient client) {
        log.info("Client disconnected: {}", client.getSessionId());
    }

    // 채팅방 생성
    @Operation(summary = "채팅방 생성" , description = "캘린더 생성 시 채팅방도 같이 생성")
    @OnEvent("createRoom")
    public ResponseEntity<ApiResponse<ChatRoomDto>> onCreateRoom(SocketIOClient client, ChatRoomCreate roomCreate ) {
        log.info("Received createRoom event: name={}, calendarId={}, userId={}",
                roomCreate.getName(), roomCreate.getCalendarId(), roomCreate.getUserId());

        client.joinRoom(roomCreate.getName());
        client.sendEvent("createRoom", roomCreate.getName());
        //checkUser(customUserDetails);
        log.info("create room successful: {}", roomCreate.getName());
        return ResponseEntity.ok(chatService.createRoom(roomCreate));
    }
    // 채팅방 입장
    //@Operation(summary = "채팅방 입장", description = "채팅방에 참여")
    //@OnEvent("joinRoom")


    // 메시지 전송

    // 메시지 읽음

    private void checkUser(CustomUserDetails customUserDetails) {
        if (customUserDetails == null) {
            ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ApiResponse.fail(ErrorCode.NOT_AUTHORIZED_USER));
        }
    }
}
