package com.github.scheduler.calender.entity;

import com.github.scheduler.auth.entity.UserEntity;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "calender")
public class CalenderEntity {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "calender_id")
    private Long calenderId;

    @Column(name = "calender_name")
    private String calenderName;

    @ManyToOne
    @JoinColumn(name = "owner_id", nullable = false)
    private UserEntity owner;

    @OneToMany(mappedBy = "calendar", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<UserCalenderEntity> userCalendars = new ArrayList<>();
}
