package com.github.scheduler.calendar.entity;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.github.scheduler.auth.entity.UserEntity;
import jakarta.persistence.*;
import lombok.*;
import net.minidev.json.annotate.JsonIgnore;

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
    @JsonBackReference
    private CalendarEntity calendar;

    @Enumerated(EnumType.STRING)
    private CalendarRole role; // OWNER, MEMBER
}
