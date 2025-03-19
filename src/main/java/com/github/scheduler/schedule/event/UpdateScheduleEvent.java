package com.github.scheduler.schedule.event;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@AllArgsConstructor
@RequiredArgsConstructor
public class UpdateScheduleEvent {
    private final Long scheduleId;
    private final String message;
    private final boolean success;
}
