package com.github.scheduler.alarm.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.datatype.jsr310.deser.LocalDateTimeDeserializer;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateTimeSerializer;
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
    @JsonSerialize(using = LocalDateTimeSerializer.class)
    @JsonDeserialize(using = LocalDateTimeDeserializer.class)
    @JsonFormat(shape= JsonFormat.Shape.STRING, pattern="yyyy-MM-dd HH:mm")
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
