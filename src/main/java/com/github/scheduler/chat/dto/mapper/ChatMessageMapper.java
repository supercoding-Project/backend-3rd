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
                .senderName(chatMessage.getSendUser().getUsername())
                .message(chatMessage.getMessage())
                .createdAt(chatMessage.getCreatedAt())
                .build();
    }
    public static List<ChatMessageDto> toChatMessageDtoPage(List<ChatMessage> chatMessages) {
        return chatMessages.stream().map(ChatMessageMapper::toChatMessageDto).toList();
    }
}
