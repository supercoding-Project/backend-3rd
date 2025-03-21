package com.github.scheduler.chat.dto;

import com.github.scheduler.chat.entity.ChatRoom;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
@AllArgsConstructor
public class ChatMessageDto {
    private Long messageId;
    private ChatRoom chatRoom;
    private Long senderId;
    private String message;
    private LocalDateTime createdAt;
}
