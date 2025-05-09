package com.github.scheduler.calendar.entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.github.scheduler.auth.entity.UserEntity;
import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "calendar")
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
public class CalendarEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "calendar_id")
    private Long calendarId;

    @Column(name = "calendar_name", nullable = false)
    private String calendarName;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "owner_id", nullable = false)
    private UserEntity owner;

    @Column(name = "calendar_description")
    private String calendarDescription;

    @Column(name = "calendar_type", nullable = false)
    @Enumerated(EnumType.STRING)
    private CalendarType calendarType;

    @Column(name = "calendar_color")
    private String calendarColor;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @OneToMany(mappedBy = "calendarEntity", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<UserCalendarEntity> userCalendars;
}
