package com.github.scheduler.chat.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ChatRoomCreate {

    @Schema(description = "채팅방 이름", example = "슈퍼코딩 BE")
    private String name;

    @Schema(description = "캘린더 ID", example = "1")
    private Long calendarId;

    @Schema(description = "유저 ID", example = "1")
    private Long userId;
}
