package com.github.scheduler.chat.dto;

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
    private LocalDateTime joinedAt;
}
