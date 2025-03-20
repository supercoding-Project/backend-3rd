package com.github.scheduler.admin.dto.schedule;

import com.github.scheduler.schedule.entity.ScheduleEntity;
import lombok.AllArgsConstructor;
import lombok.Getter;
import java.time.LocalDateTime;




@Getter
@AllArgsConstructor
public class ScheduleSimpleDTO {
    private Long scheduleId;
    private String title;
    private LocalDateTime startTime;
    private LocalDateTime endTime;

    public static ScheduleSimpleDTO from(ScheduleEntity schedule) {
        return new ScheduleSimpleDTO(
                schedule.getScheduleId(),
                schedule.getTitle(),
                schedule.getStartTime(),
                schedule.getEndTime()
        );
    }
}
