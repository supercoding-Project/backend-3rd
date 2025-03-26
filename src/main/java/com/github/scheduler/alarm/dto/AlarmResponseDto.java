package com.github.scheduler.alarm.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class AlarmResponseDto {
    private Long id;
    private Long userId;
    private String type;
    private boolean isChecked;
}
