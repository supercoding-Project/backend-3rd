package com.github.scheduler.admin.dto.user;


import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@AllArgsConstructor
@NoArgsConstructor
public class AdminUserUpdateDTO {
    private String username;
    private String email;
    private String phone;
    private Boolean resetPassword;

}
