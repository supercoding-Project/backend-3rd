package com.github.scheduler.admin.dto.user;

import com.github.scheduler.auth.entity.UserEntity;
import com.github.scheduler.auth.entity.UserStatus;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class AdminUserResponseDTO {
    private Long id;
    private String name;
    private String email;
    private UserStatus status;

    public static AdminUserResponseDTO from(UserEntity user) {
        return new AdminUserResponseDTO(
                user.getUserId(),
                user.getUsername(),
                user.getEmail(),
                user.getStatus()
        );
    }
}
