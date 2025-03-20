package com.github.scheduler.schedule.event;

public record DeleteScheduleEvent(Long scheduleId, String message, boolean success) {
}
