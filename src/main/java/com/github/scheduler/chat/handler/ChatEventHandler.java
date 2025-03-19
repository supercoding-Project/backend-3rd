package com.github.scheduler.chat.handler;

import com.corundumstudio.socketio.SocketIOClient;
import com.corundumstudio.socketio.SocketIOServer;
import com.corundumstudio.socketio.annotation.OnConnect;
import com.corundumstudio.socketio.annotation.OnDisconnect;
import com.corundumstudio.socketio.annotation.OnEvent;
import com.github.scheduler.chat.dto.*;
import com.github.scheduler.chat.service.ChatService;
import com.github.scheduler.global.config.auth.custom.CustomUserDetails;
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
        log.info("Client connected: {}", client.getSessionId());
    }
    // 클라이언트 연결 종료 시 실행
    @OnDisconnect
    public void onDisconnect(SocketIOClient client) {
        log.info("Client disconnected: {}", client.getSessionId());
    }

    // 채팅방 생성
    @OnEvent("createRoom")
    public ResponseEntity<ApiResponse<ChatRoomDto>> onCreateRoom(
            @AuthenticationPrincipal CustomUserDetails customUserDetails,
            SocketIOClient client, ChatRoomCreate roomCreate ) {
        // user check
        checkUser(customUserDetails);
        log.info("Received createRoom event: name={}, calendarId={}, userId={}",
                roomCreate.getName(), roomCreate.getCalendarId(), roomCreate.getUserId());

        // room entity 추가
        ApiResponse<ChatRoomDto> chatRoomDto = chatService.createRoom(roomCreate,client);

        return ResponseEntity.ok(chatRoomDto);
    }
    // 채팅방 입장
    //@Operation(summary = "채팅방 입장", description = "채팅방에 참여")
    @OnEvent("joinRoom")
    public ResponseEntity<ApiResponse<ChatRoomUserDto>> onJoinRoom(
            @AuthenticationPrincipal CustomUserDetails customUserDetails,
            SocketIOClient client, ChatRoomJoinRequest request){

        checkUser(customUserDetails);
        log.info("Received joinRoom event: roomId={}, userId={}", request.getRoomId(), request.getUserId());

        ApiResponse<ChatRoomUserDto> chatRoomDto = chatService.joinRoom(request,client);

        return ResponseEntity.ok(chatRoomDto);

    }

    // 메시지 전송
    @OnEvent("sendMessage")
    public ResponseEntity<ApiResponse<ChatMessageDto>> onSendMessage(
            @AuthenticationPrincipal CustomUserDetails customUserDetails,
            SocketIOClient client, ChatMessageRequest request ){

        checkUser(customUserDetails);
        log.info("Received sendMessage event: roomId={}, userId={}", request.getRoomId(), request.getUserId());

        ApiResponse<ChatMessageDto> chatMessage = chatService.sendMessage(client,request);

    }


    // 메시지 읽음

    private void checkUser(CustomUserDetails customUserDetails) {
        if (customUserDetails == null) {
            ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ApiResponse.fail(ErrorCode.NOT_AUTHORIZED_USER));
        }
    }
}
