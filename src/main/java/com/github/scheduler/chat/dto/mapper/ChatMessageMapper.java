package com.github.scheduler.chat.dto.mapper;

import com.github.scheduler.chat.dto.ChatMessageDto;
import com.github.scheduler.chat.entity.ChatMessage;
import org.springframework.data.domain.Page;

import java.util.List;

public class ChatMessageMapper {
    public static ChatMessageDto toChatMessageDto(ChatMessage chatMessage) {
        return ChatMessageDto.builder()
                .messageId(chatMessage.getId())
                .roomName(chatMessage.getChatRoom().getName())
                .chatRoomId(chatMessage.getChatRoom().getId())
                .calendarId(chatMessage.getChatRoom().getCalendar().getCalendarId())
                .senderId(chatMessage.getSendUser().getUserId())
                .message(chatMessage.getMessage())
                .createdAt(chatMessage.getCreatedAt())
                .build();
    }
    public static Page<ChatMessageDto> toChatMessageDtoPage(Page<ChatMessage> chatMessages) {
        return chatMessages.map(ChatMessageMapper::toChatMessageDto);
    }
}
