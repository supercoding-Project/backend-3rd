package com.github.scheduler.auth.entity;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum UserStatus {
    // 유저 상태
    ACTIVE("ACTIVE"),
    DELETED("DELETED");

    private final String status;
}
