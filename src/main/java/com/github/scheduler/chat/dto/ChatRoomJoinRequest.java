package com.github.scheduler.chat.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class ChatRoomJoinRequest {
    private Long userId;
    private Long roomId;
}
