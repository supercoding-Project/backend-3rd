package com.github.scheduler.calender.entity;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public enum CalenderRole {
    // 관리자 권한 부여
    OWNER("OWNER"),
    MEMBER("MEMBER");

    private final String type;
}
