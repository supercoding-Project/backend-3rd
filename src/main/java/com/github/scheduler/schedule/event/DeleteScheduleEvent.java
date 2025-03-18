package com.github.scheduler.schedule.event;


import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public class DeleteScheduleEvent {
    private final Long scheduleId;
    private final String message;
}
