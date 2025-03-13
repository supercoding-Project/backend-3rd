package com.github.scheduler.calendar.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;


@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CalendarResponseDto {
    private Long calendarId;
    private String calendarName;
    private String calendarType;
    private String calendarRole;
    private LocalDateTime createdAt;
}
