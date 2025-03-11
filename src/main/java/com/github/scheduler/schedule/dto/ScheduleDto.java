package com.github.scheduler.schedule.dto;

import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@ToString
@AllArgsConstructor
@NoArgsConstructor
public class ScheduleDto {

    private Long scheduleId;
    private Long createUserId; // 팀 공유 캘린더에서 일정을 등록한 사용자
    private String title;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private RepeatScheduleDto repeatSchedule; //일정 반복 설정
    private String location;
    private String memo;
    private String teamCode; //TeamCode = null -> personalSchedule
    private Long chatRoomId; //채팅방 이동
    private String status; //일정 상태
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

}
