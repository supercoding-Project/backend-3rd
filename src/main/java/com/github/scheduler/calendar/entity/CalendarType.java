package com.github.scheduler.calendar.entity;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public enum CalendarType {
    // 캘린더 타입 설정
    PERSONAL("PERSONAL"),
    SHARED("SHARED"),
    TODO("TODO");

    private final String type;
}
