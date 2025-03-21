package com.github.scheduler.chat.event;

import com.corundumstudio.socketio.SocketIOClient;
import com.github.scheduler.chat.dto.ChatRoomDto;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public class ChatRoomCreateEvent {
    private final ChatRoomDto chatRoomDto;
    private final SocketIOClient client;

}
