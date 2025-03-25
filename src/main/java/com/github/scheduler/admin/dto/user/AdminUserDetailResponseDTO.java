package com.github.scheduler.admin.dto.user;

import com.github.scheduler.auth.entity.UserEntity;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@AllArgsConstructor
@NoArgsConstructor
public class AdminUserDetailResponseDTO {
    private Long id;
    private String username;
    private String password;
    private String email;
    private String phone;
    private String role;


    public static AdminUserDetailResponseDTO from(UserEntity user) {
        return new AdminUserDetailResponseDTO(
                user.getUserId(),
                user.getUsername(),
                user.getPassword(),
                user.getEmail(),
                user.getPhone(),
                user.getRole().toString()
        );
    }
}
