package com.github.scheduler.calendar.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Getter
@NoArgsConstructor
public class CalendarMemberDeleteRequestDto {
    private List<String> targetEmails;
}
