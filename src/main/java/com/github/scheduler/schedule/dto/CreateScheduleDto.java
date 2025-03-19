package com.github.scheduler.schedule.dto;

import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class CreateScheduleDto {
    private Long createUserId;
    private Long calendarId;
    private String title;
    private String location;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private RepeatScheduleDto repeatSchedule;
    private String memo;
}
