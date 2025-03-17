package com.github.scheduler.chat.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ChatRoomDto {
    private Long chatRoomId;
    private String roomName;
    private Long calendarId;
    private LocalDateTime createdAt;
}
