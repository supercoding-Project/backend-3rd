package com.github.scheduler.mypage.dto;
import lombok.*;

import lombok.AllArgsConstructor;

@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class UserDto {
    private Long id;
    private String name;
    private String email;
    private String userImageUrl;
}