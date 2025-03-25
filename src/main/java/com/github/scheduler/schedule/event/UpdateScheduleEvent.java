package com.github.scheduler.schedule.event;


import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@AllArgsConstructor
@RequiredArgsConstructor
public class UpdateScheduleEvent {
    private Long ScheduleId;
    private String message;
    private boolean success;
}
