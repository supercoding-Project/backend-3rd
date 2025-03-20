package com.github.scheduler.schedule.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@AllArgsConstructor
public class DeleteScheduleDto {
    private Long scheduleId;
    private String message;
}
