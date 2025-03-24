package com.github.scheduler.chat.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@AllArgsConstructor
public class ChatMessageRequest {
    private Long roomId;
    private Long sendUserId;
    private String message;
    private String fileURL;
}
