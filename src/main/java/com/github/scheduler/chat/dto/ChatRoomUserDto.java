package com.github.scheduler.chat.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateTimeSerializer;
import com.github.scheduler.chat.entity.ChatRoom;
import com.github.scheduler.chat.entity.ChatRoomUser;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
@AllArgsConstructor
public class ChatRoomUserDto {
    private Long roomId;
    private String roomName;
    private Long userId;
    private Long calendarId;
    private Long lastReadMessageId;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime joinedAt;

    public static ChatRoomUserDto toDto(ChatRoomUser chatRoomUser) {
        return  ChatRoomUserDto.builder()
                .roomId(chatRoomUser.getChatRoom().getId())
                .roomName(chatRoomUser.getChatRoom().getName())
                .userId(chatRoomUser.getUser().getUserId())
                .calendarId(chatRoomUser.getChatRoom().getCalendar().getCalendarId())
                .lastReadMessageId(chatRoomUser.getLastReadMessageId())
                .joinedAt(chatRoomUser.getJoinedAt())
                .build();

    }
}
