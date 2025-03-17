package com.github.scheduler.todo.dto;

import com.github.scheduler.calendar.entity.CalendarType;
import lombok.*;

import java.time.LocalDate;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class CreateTodoDto {
    private String todoContent; //할 일 내용
    private LocalDate todoDate; //기한 (날짜만 포함)
    private String memo;//메모
    private CalendarType calendarType;//할 일 그룹(개인인지 공유인지)
    private Long calendarId;//할 일이 속한 캘린더 Id
}
