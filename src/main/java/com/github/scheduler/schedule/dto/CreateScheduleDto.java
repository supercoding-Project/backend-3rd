package com.github.scheduler.schedule.dto;

import lombok.*;

import java.time.LocalDateTime;

@Getter
@AllArgsConstructor
public class CreateScheduleDto {
    private Long createUserId;
    private String title;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private RepeatScheduleDto repeatSchedule;
    private String location;
    private String memo;
    private String teamCode; // 팀 일정인 경우 체크박스로 선택 → 팀 코드 입력 (개인 일정이면 null)
}
