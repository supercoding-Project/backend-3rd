package com.github.scheduler.schedule.dto;

import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class ScheduleDto {

    private Long scheduleId;
    private Long createUserId;
    private Long creatorId; // 팀 공유 캘린더에서 다른 사용자가 작성한 일정 작성자의 ID
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
