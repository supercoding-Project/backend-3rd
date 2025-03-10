package com.github.scheduler.schedule.entity;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum RepeatType {

    NONE("반복 없음"),
    DAILY("매일 반복"),
    WEEKLY("매 주 반복"),
    MONTHLY("매 월 반복"),
    YEARLY("매 년 반복");

    private final String repeatStatus;
}
