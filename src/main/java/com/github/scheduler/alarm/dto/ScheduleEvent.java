package com.github.scheduler.alarm.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import java.time.LocalDateTime;
import java.time.LocalDate;

@Data
@AllArgsConstructor
public class ScheduleEvent {
    private Long userId;
    private Long calendarId;
    private Long scheduleId;
    private String type;
    private LocalDateTime startTime;
    private String repeatType;
    private LocalDate repeatEndDate;
}
