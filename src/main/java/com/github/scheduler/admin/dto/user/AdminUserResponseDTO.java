package com.github.scheduler.admin.dto.user;

import com.github.scheduler.auth.entity.UserEntity;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@AllArgsConstructor
@NoArgsConstructor
public class AdminUserResponseDTO {
    private Long id;
    private String name;
    private String email;

    public static AdminUserResponseDTO from(UserEntity user) {
        return new AdminUserResponseDTO(
                user.getUserId(),
                user.getUsername(),
                user.getEmail()
        );
    }
}
