package com.github.scheduler.schedule.event;


public record UpdateScheduleEvent(Long scheduleId, String message, boolean success) {
}
