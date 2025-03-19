package com.github.scheduler.calendar.entity;

import com.github.scheduler.auth.entity.UserEntity;
import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Builder
@Table(name = "user_calendar")
public class UserCalendarEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "user_calendar_id")
    private Long userCalendarID;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private UserEntity userEntity;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "calendar_id", nullable = false)
    private CalendarEntity calendarEntity;

    @Column(name = "role", nullable = false)
    @Enumerated(EnumType.STRING) // Enum 값을 문자열로 저장
    private CalendarRole role;

    @Column(name = "joined_at", nullable = false)
    private LocalDateTime joinedAt;
}