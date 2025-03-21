package com.github.scheduler.calendar.dto;

import com.github.scheduler.calendar.entity.CalendarRole;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class CalendarMemberResponseDto {
    private String email;
    private String username;
    private CalendarRole role;
}
