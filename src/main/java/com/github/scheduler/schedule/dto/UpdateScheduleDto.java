package com.github.scheduler.schedule.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class UpdateScheduleDto {
    private String title;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private RepeatScheduleDto repeatSchedule;
    private String location;
    private String todoList;
    private String calendarId;
}
