package com.github.scheduler.chat.event;

import com.corundumstudio.socketio.SocketIOClient;
import com.github.scheduler.chat.dto.ChatMessageDto;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public class ChatMessageSendEvent {
    private final ChatMessageDto chatMessageDto;
    private final SocketIOClient client;
}
