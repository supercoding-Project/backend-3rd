package com.github.scheduler.alarm.dto;

import com.github.scheduler.alarm.entity.SchedulerAlarmEntity;
import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class SchedulerAlarmDto {
    private Long userId;
    private Long calendarId;
    private Long eventId;
    private String type;
    private boolean isRead;
}
