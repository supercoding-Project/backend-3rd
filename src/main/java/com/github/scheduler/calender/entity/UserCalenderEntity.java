package com.github.scheduler.calender.entity;

import com.github.scheduler.auth.entity.UserEntity;
import jakarta.persistence.*;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Builder
@Table(name = "user_calender")
public class UserCalenderEntity {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "user_calender_id")
    private Long userCalenderID;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private UserEntity userEntity;

    @ManyToOne
    @JoinColumn(name = "calendar_id", nullable = false)
    private CalenderEntity calenderEntity;

    @Enumerated(EnumType.STRING)
    private CalenderRole role; // OWNER, MEMBER
}
