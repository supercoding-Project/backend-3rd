package com.github.scheduler.auth.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class LoginDto {
    // 로그인 DTO
    private String email;
    private String password;
}
