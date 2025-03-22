package com.github.scheduler.schedule.event;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@AllArgsConstructor
@RequiredArgsConstructor
public class ScheduleCreatedEvent {
    private Long scheduleId;
    private String message;

}
