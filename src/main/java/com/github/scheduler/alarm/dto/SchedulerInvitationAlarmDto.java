package com.github.scheduler.alarm.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
public class SchedulerInvitationAlarmDto {
    private Long userId;
    private Long calendarId;
    private String type;
    private boolean isChecked;
}
