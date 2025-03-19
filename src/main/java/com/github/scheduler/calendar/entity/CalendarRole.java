package com.github.scheduler.calendar.entity;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public enum CalendarRole {
    OWNER("OWNER"),
    MEMBER("MEMBER");

    private final String type;
}
