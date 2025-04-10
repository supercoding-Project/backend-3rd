package com.github.scheduler.alarm.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class ResponseAlarmDto {
    private Long id;
    private String type;
    private String calendarName;
    private String eventName;
    private String location;
    private int members;
    private LocalDateTime eventTime;
    private boolean read;

    @Override
    public String toString() {
        return "ResponseAlarmDto{" +
                "id=" + id +
                ", type='" + type + '\'' +
                ", calendarName='" + calendarName + '\'' +
                ", eventName='" + eventName + '\'' +
                ", location='" + location + '\'' +
                ", members=" + members +
                ", eventTime=" + eventTime +
                ", read=" + read +
                '}';
    }
}
