package com.github.scheduler.todo.dto;

import com.github.scheduler.calendar.entity.CalendarType;
import com.github.scheduler.schedule.dto.RepeatScheduleDto;
import com.github.scheduler.schedule.entity.RepeatType;
import lombok.*;

import java.time.LocalDate;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class TodoCreateDto {
    private Long todoId;
    private Long calendarId;//할 일이 속한 캘린더 Id
    private String todoContent; //할 일 내용
    private LocalDate todoDate; //기한 (날짜만 포함)
    private RepeatScheduleDto repeatSchedule;
    private String memo;//메모
    private CalendarType calendarType;//할 일 그룹(개인인지 공유인지)
}
