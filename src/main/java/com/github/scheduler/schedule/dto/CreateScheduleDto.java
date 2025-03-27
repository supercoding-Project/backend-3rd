package com.github.scheduler.schedule.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class CreateScheduleDto {
    @Schema(example = "0")
    private Long createUserId;

    @Schema(example = "0")
    private Long calendarId;

    @Schema(example = "일정 제목")
    private String title;

    @Schema(example = "일정 장소")
    private String location;

    @Schema(example = "메모")
    private String memo;

    @Schema(example = "YYYY-MM-dd hh:mm:ss")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime startTime;

    @Schema(example = "YYYY-MM-dd hh:mm:ss")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime endTime;

    @Schema(description = "반복 설정")
    private RepeatScheduleDto repeatSchedule;

    @Schema(example = "[mention user_id]")
    private List<Long> mentionUserIds;

}
