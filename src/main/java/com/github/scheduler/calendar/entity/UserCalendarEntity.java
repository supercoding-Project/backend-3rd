package com.github.scheduler.calendar.entity;

import com.fasterxml.jackson.annotation.JsonBackReference;
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
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "user_calendar_id")
    private Long userCalendarID;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private UserEntity userEntity;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "calendar_id", nullable = false)
    @JsonBackReference
    private CalendarEntity calendarEntity;

    @Column(name = "role")
    private CalendarRole role; // OWNER, MEMBER

    @Column(name = "joined_at")
    private LocalDateTime joinedAt;
}
