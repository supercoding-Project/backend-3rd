package com.github.scheduler.admin.dto.schedule;

import com.github.scheduler.auth.entity.UserEntity;
import com.github.scheduler.schedule.entity.SchedulerEntity;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.List;

@Getter
@AllArgsConstructor
public class ResponseUserScheduleListDTO {
    private Long userId;
    private String username;
    private List<ScheduleSimpleDTO> schedules;

    public static ResponseUserScheduleListDTO from(UserEntity user, List<SchedulerEntity> schedules) {
        return new ResponseUserScheduleListDTO(
                user.getUserId(),
                user.getUsername(),
                schedules.stream().map(ScheduleSimpleDTO::from).toList()
        );
    }

}
