package com.github.scheduler.admin.dto.user;

import com.github.scheduler.auth.entity.UserStatus;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class AdminUserStatusUpdateDTO {
    private String email;
    private UserStatus status;
}
