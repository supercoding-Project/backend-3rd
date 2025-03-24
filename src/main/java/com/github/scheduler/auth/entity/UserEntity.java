package com.github.scheduler.auth.entity;

import com.github.scheduler.calendar.entity.UserCalendarEntity;
import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Builder
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "users")
public class    UserEntity {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "user_id")
    private Long userId;

    @Column(name = "email", nullable = false)
    private String email;

    @Column(name = "password", nullable = false)
    private String password;

    @Column(name = "username", nullable = false)
    private String username;

    @Column(name = "phone", nullable = false)
    private String phone;

    // OAuth2
    @Column(name = "provider")
    private String provider;

    // OAuth2
    @Column(name = "provider_id")
    private String providerId;

    @Column(name = "role")
    @Enumerated(EnumType.STRING)
    private Role role;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @OneToOne(mappedBy = "userEntity", cascade = CascadeType.ALL)
    private UserImageEntity userImageEntity;

    @OneToOne(mappedBy = "userEntity", cascade = CascadeType.ALL)
    private RefreshTokenEntity refreshToken;

    @OneToMany(mappedBy = "userEntity", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<UserCalendarEntity> userCalendars = new ArrayList<>();

    public UserEntity update(String username, UserImageEntity userImageEntity) {
        this.username = username;
        this.userImageEntity = userImageEntity;

        return this;
    }
}
