package com.github.scheduler.schedule.dto;

import lombok.Getter;

import java.time.LocalDate;

@Getter
public class RepeatScheduleDto {
    private String repeatType; //"NONE", "DAILY", "WEEKLY", "MONTHLY", "YEARLY"
    private int repeatInterval; // 반복 간격
    private LocalDate repeatEndDate; // 반복 종료 날짜
}
