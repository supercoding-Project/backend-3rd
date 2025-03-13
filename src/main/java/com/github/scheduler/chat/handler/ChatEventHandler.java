package com.github.scheduler.chat.handler;

import com.corundumstudio.socketio.SocketIOClient;
import com.corundumstudio.socketio.SocketIOServer;
import com.corundumstudio.socketio.annotation.OnEvent;
import com.github.scheduler.chat.dto.ChatRoomCreate;
import com.github.scheduler.chat.service.ChatService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ChatEventHandler {
    private final SocketIOServer server;
    private final ChatService chatService;

    // 클라이언트 연결 시 실행

    // 클라이언트 연결 종료 시 실행

    // 채팅방 생성
    @OnEvent("createRoom")
    public void onCreateRoom(SocketIOClient client, ChatRoomCreate roomCreate ) {
        client.joinRoom(roomCreate.getName());
    }
    // 채팅방 입장

    // 메시지 전송

    // 메시지 읽음

}
