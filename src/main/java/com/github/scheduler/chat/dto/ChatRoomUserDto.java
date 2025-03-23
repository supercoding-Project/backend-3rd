package com.github.scheduler.chat.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateTimeSerializer;
import com.github.scheduler.chat.entity.ChatRoom;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
@AllArgsConstructor
public class ChatRoomUserDto {
    private ChatRoom chatRoom;
    private Long userId;
    private Long lastReadMessageId;

    @JsonSerialize(using = LocalDateTimeSerializer.class)
    private LocalDateTime joinedAt;
}
