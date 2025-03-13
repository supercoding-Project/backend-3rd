package com.github.scheduler.schedule.dto;

import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@ToString
@AllArgsConstructor
@NoArgsConstructor
public class ScheduleDto {

    private Long scheduleId; // 일정 Id
    private Long createUserId; // 팀 공유 캘린더에서 일정을 등록한 사용자
    private String title; // 일정 제목
    private LocalDateTime startTime; //일정 시작 시간
    private LocalDateTime endTime; // 일정 종료 시간
    private RepeatScheduleDto repeatSchedule; //일정 반복 설정
    private String location; // 일정 장소
    private String todoList; // 메모
    private String calendarId; // calendarId = null -> 개인일정, (캘린더 = 팀)
    private String status; //일정 상태

}
