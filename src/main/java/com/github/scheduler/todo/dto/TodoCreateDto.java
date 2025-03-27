package com.github.scheduler.todo.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.github.scheduler.calendar.entity.CalendarType;
import com.github.scheduler.schedule.dto.RepeatScheduleDto;
import com.github.scheduler.schedule.entity.RepeatType;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

import java.time.LocalDate;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class TodoCreateDto {
    @Schema(example = "0")
    private Long todoId;

    @Schema(example = "0")
    private Long calendarId;//할 일이 속한 캘린더 Id

    @Schema(example = "할 일")
    private String todoContent; //할 일 내용

    @Schema(example = "메모")
    private String memo;//메모

    @Schema(example = "YYYY-MM-dd")
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate todoDate; //기한 (날짜만 포함)

    @Schema(description = "할 일 반복 설정")
    private RepeatScheduleDto repeatSchedule;
}
