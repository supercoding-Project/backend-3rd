package com.github.scheduler.schedule.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Table(name = "schedules")
public class SchedulerEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "schedule_id")
    private Long scheduleId;

    @Column(name = "create_user_id", nullable = false)
    private Long createUserId;

    @Column(nullable = false)
    private String title;

    @Column(name = "start_time", nullable = false)
    private LocalDateTime startTime;

    @Column(name = "end_time", nullable = false)
    private LocalDateTime endTime;

    @Enumerated(EnumType.STRING)
    @Column(name = "repeat_type", nullable = false)
    private RepeatType repeatType = RepeatType.NONE;

    @Column(name = "repeat_interval", nullable = false)
    private int repeatInterval = 1; //년 경우 1이면 1년마다 반복, 월 경우 1이면 1달 마다 반복, 주 경우 1이면 매 주 반복

    @Column(name = "repeat_end_date")
    private LocalDate repeatEndDate;

    private String location;

    @Column(columnDefinition = "TEXT")
    private String memo;

    @Column(name = "team_code", length = 50)
    private String teamCode; // 팀 일정이면 값 존재, 개인 일정이면 null

    @Enumerated(EnumType.STRING)
    @Column(name = "schedule_status", nullable = false)
    private ScheduleStatus scheduleStatus = ScheduleStatus.SCHEDULED;

    @Column(name = "chat_room_id")
    private Long chatRoomId;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
}
