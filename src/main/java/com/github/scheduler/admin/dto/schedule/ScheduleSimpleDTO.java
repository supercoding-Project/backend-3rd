package com.github.scheduler.admin.dto.schedule;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.github.scheduler.schedule.entity.ScheduleEntity;
import lombok.AllArgsConstructor;
import lombok.Getter;
import java.time.LocalDateTime;




@Getter
@AllArgsConstructor
public class ScheduleSimpleDTO {
    private Long scheduleId;
    private String title;
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
    private LocalDateTime startTime;
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
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
