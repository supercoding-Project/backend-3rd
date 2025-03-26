package com.github.scheduler.chat.dto;

import com.github.scheduler.chat.entity.ChatRoom;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.ToString;

import java.time.LocalDateTime;

@Getter
@Builder
@AllArgsConstructor
@ToString
public class ChatMessageDto {
    private Long messageId;
    private String roomName;
    private Long chatRoomId;
    private Long calendarId;
    private Long senderId;
    private String message;
    private LocalDateTime createdAt;
}
