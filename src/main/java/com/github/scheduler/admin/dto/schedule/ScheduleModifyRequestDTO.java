package com.github.scheduler.admin.dto.schedule;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Getter
@AllArgsConstructor
@NoArgsConstructor
public class ScheduleModifyRequestDTO {
    private String title;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private String location;
    private String memo;

}
