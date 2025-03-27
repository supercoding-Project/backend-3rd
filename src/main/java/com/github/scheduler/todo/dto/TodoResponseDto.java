package com.github.scheduler.todo.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.github.scheduler.schedule.dto.RepeatScheduleDto;
import lombok.*;

import java.time.LocalDate;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class TodoResponseDto {
    private Long todoId;
    private String calendarId; //할 일이 속한 캘린더 타입
    private Long createUserId;
    private String todoContent; //할 일
    private String memo;

    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate todoDate; //기한

    private RepeatScheduleDto repeatSchedule; //반복 설정
}
