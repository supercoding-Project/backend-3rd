package com.github.scheduler.schedule.dto;

import com.github.scheduler.schedule.entity.ScheduleStatus;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class CreateScheduleDto {
    private Long createUserId;
    private String title;
    private String location;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private RepeatScheduleDto repeatSchedule;
    private String memo;
    private Long calendarId;
}
