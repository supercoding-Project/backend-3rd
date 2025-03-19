package com.github.scheduler.admin.dto.schedule;

import lombok.AllArgsConstructor;
import lombok.Getter;
import java.time.LocalDateTime;
import com.github.scheduler.schedule.entity.SchedulerEntity;



@Getter
@AllArgsConstructor
public class ScheduleSimpleDTO {
    private Long scheduleId;
    private String title;
    private LocalDateTime startTime;
    private LocalDateTime endTime;

    public static ScheduleSimpleDTO from(SchedulerEntity schedule) {
        return new ScheduleSimpleDTO(
                schedule.getScheduleId(),
                schedule.getTitle(),
                schedule.getStartTime(),
                schedule.getEndTime()
        );
    }
}
