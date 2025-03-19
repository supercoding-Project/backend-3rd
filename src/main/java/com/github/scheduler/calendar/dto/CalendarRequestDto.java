package com.github.scheduler.calendar.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class CalendarRequestDto {
    @NotBlank(message = "캘린더 이름은 필수 입력값입니다.")
    private String calendarName;

    private String CalendarDescription;

    @NotBlank(message = "캘린더 타입은 필수 입력값입니다.")
    private String calendarType;
}
