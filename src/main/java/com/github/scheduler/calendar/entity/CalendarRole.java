package com.github.scheduler.calendar.entity;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public enum CalendarRole {
    // 관리자 권한 부여
    OWNER("OWNER"),
    MEMBER("MEMBER");

    private final String type;
}
