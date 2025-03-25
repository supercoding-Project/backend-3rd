package com.github.scheduler.schedule.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.*;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class CreateScheduleDto {
    private Long createUserId;
    private Long scheduleId;
    private Long calendarId;
    private String title;
    private String location;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime startTime;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime endTime;
    private RepeatScheduleDto repeatSchedule;
    private String memo;
    private List<Long> mentionUserIds;
}
