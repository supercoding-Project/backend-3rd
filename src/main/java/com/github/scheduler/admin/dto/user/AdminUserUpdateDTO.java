package com.github.scheduler.admin.dto.user;


import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class AdminUserUpdateDTO {
    private String username;
    private String email;
    private String phone;
    private Boolean resetPassword;

}
