package com.github.scheduler.chat.controller;

import com.corundumstudio.socketio.SocketIOServer;
import com.github.scheduler.chat.dto.ChatMessageRequest;
import com.github.scheduler.chat.dto.ChatRoomCreate;
import com.github.scheduler.chat.dto.ChatRoomJoinRequest;
import com.github.scheduler.chat.service.ChatService;
import com.github.scheduler.global.config.chat.SocketSecurityInterceptor;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import org.springframework.stereotype.Controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;


@Slf4j
@Controller
@RequiredArgsConstructor
public class ChatSocketController {
    private final ChatService chatService;
    private final SocketIOServer server;
    private final SocketSecurityInterceptor securityInterceptor;

    @PostConstruct
    public void init(){
        server.start();
        // 연결,해제 이벤트 리스너
        server.addConnectListener(securityInterceptor);
        server.addDisconnectListener(chatService::onDisconnect);
        // 채팅방 이벤트 리스너
        server.addEventListener("createRoom",ChatRoomCreate.class,chatService::createRoom);
        server.addEventListener("joinRoom", ChatRoomJoinRequest.class,chatService::joinRoom);
        // 메시지 이벤트 리스너
        server.addEventListener("sendMessage", ChatMessageRequest.class,chatService::sendMessage);
    }

    @PreDestroy
    public void stopServer() {
        server.stop();

    }
}
