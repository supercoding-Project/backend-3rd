package com.github.scheduler.chat.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.Optional;

@Getter
@AllArgsConstructor
@NoArgsConstructor
public class ChatMessageRequest {
    private Long roomId;
    private Long sendUserId;
    private String message;
}
