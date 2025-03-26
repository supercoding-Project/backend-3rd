package com.github.scheduler.chat.dto.mapper;

import com.github.scheduler.chat.dto.ChatRoomUserDto;
import com.github.scheduler.chat.entity.ChatRoomUser;

import java.util.List;
import java.util.stream.Collectors;

public class ChatRoomUserMapper {
    public static ChatRoomUserDto toChatRoomUserDto(ChatRoomUser chatRoomUser) {
        return  ChatRoomUserDto.builder()
                .roomId(chatRoomUser.getChatRoom().getId())
                .roomName(chatRoomUser.getChatRoom().getName())
                .userId(chatRoomUser.getUser().getUserId())
                .calendarId(chatRoomUser.getChatRoom().getCalendar().getCalendarId())
                .lastReadMessageId(chatRoomUser.getLastReadMessageId())
                .joinedAt(chatRoomUser.getJoinedAt())
                .build();
    }
    public static List<ChatRoomUserDto> toChatRoomUserDtoList(List<ChatRoomUser> chatRoomUsers) {
        return chatRoomUsers.stream()
                .map(ChatRoomUserMapper::toChatRoomUserDto)
                .toList();
    }
}
