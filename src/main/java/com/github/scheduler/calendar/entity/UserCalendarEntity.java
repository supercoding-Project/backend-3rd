package com.github.scheduler.calendar.entity;

import com.github.scheduler.auth.entity.UserEntity;
import jakarta.persistence.*;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Builder
@Table(name = "user_calendar")
public class UserCalendarEntity {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "user_calendar_id")
    private Long userCalendarID;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private UserEntity userEntity;

    @ManyToOne
    @JoinColumn(name = "calendar_id", nullable = false)
    private CalendarEntity calendar;

    @Enumerated(EnumType.STRING)
    private CalendarRole role; // OWNER, MEMBER
}
