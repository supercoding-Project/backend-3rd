package com.github.scheduler.schedule.dto;

import lombok.*;

import java.time.LocalDate;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class RepeatScheduleDto {
    private String repeatType; //"NONE", "DAILY", "WEEKLY", "MONTHLY", "YEARLY"
    private Integer repeatInterval; // 반복 간격
    private LocalDate repeatEndDate; // 반복 종료 날짜

}
