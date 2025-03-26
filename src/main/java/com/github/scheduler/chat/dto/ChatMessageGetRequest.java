package com.github.scheduler.chat.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.joda.time.DateTime;

import java.time.LocalDateTime;

@Getter
@AllArgsConstructor
@NoArgsConstructor
public class ChatMessageGetRequest {
    @Schema(name = "roomId", description = "채팅방ID", example = "1")
    private Long roomId;
    @Schema(name = "searchDateTime", description = "현재부터 조회할 기간" , example = "2025-03-25 11:00:00")
    private LocalDateTime searchDateTime;

}
