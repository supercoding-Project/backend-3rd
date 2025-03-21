package com.github.scheduler.schedule.entity;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum ScheduleStatus {
    SCHEDULED("예정된 일정"),
    COMPLETED("완료된 일정"),
    CANCELLED("취소된 일정"),
    DELETED("삭제된 일정");

    private final String scheduleStatus;
}
