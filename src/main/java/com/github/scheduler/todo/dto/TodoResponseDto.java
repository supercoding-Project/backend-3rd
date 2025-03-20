package com.github.scheduler.todo.dto;

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
    private Long createUserId;
    private String todoContent; //할 일
    private LocalDate todoDate; //기한
    private RepeatScheduleDto repeatSchedule; //반복 설정
    private String memo;
    private String calendarId; //할 일이 속한 캘린더 타입
}
