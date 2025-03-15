package com.github.scheduler.auth.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Builder
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "users")
public class    UserEntity {
    // users 테이블
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

    @Column(name = "status")
    @Enumerated(EnumType.STRING)
    private UserStatus status;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "deleted_at")
    private LocalDateTime deletedAt;

    @OneToOne(mappedBy = "userEntity", cascade = CascadeType.ALL)
    private UserImageEntity userImageEntity;

    @OneToOne(mappedBy = "userEntity", cascade = CascadeType.ALL)
    private RefreshTokenEntity refreshToken;

    public UserEntity update(String username, UserImageEntity userImageEntity) {
        this.username = username;
        this.userImageEntity = userImageEntity;

        return this;
    }
}
