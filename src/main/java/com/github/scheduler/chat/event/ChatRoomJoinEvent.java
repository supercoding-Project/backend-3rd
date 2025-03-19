package com.github.scheduler.chat.event;

import com.corundumstudio.socketio.SocketIOClient;
import com.github.scheduler.chat.dto.ChatRoomUserDto;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public class ChatRoomJoinEvent {
    private final ChatRoomUserDto chatRoomUserDto;
    private final SocketIOClient client;
}
