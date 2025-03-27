package com.github.scheduler.alarm.dto;

import com.github.scheduler.alarm.entity.SchedulerAlarmEntity;
import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class SchedulerAlarmDto {
    private Long alarmId;
    private Long userId;
    private Long calendarId;
    private Long eventId;
    private String type;
    private boolean isRead;

    @Override
    public String toString() {
        return "SchedulerAlarmDto{" +
                "alarmId=" + alarmId +
                ", userId=" + userId +
                ", calendarId=" + calendarId +
                ", eventId=" + eventId +
                ", type='" + type + '\'' +
                ", isRead=" + isRead +
                '}';
    }
}
