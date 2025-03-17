package com.github.scheduler.schedule.dto;

import com.github.scheduler.auth.entity.UserEntity;
import com.github.scheduler.calendar.entity.UserCalendarEntity;
import lombok.*;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
@Builder
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
    private String memo; //일정 설명
    private String calendarId;
    private String status; //일정 상태
    private List<UserCalendarEntity> sharedUsers;  // 공유 캘린더인 경우 소속 유저 목록
}
