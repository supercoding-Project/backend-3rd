package com.github.scheduler.calendar.entity;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import com.github.scheduler.global.exception.AppException;
import com.github.scheduler.global.exception.ErrorCode;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Arrays;

@AllArgsConstructor
@Getter
public enum CalendarType {
    PERSONAL("PERSONAL"),
    SHARED("SHARED"),
    TODO("TODO");

    private final String type;

    // JSON 직렬화 시 문자열로 변환
    @JsonValue
    public String getType() {
        return type;
    }

    // JSON -> Enum 변환 (문자열 입력 처리)
    @JsonCreator
    public static CalendarType fromString(String type) {
        return Arrays.stream(CalendarType.values())
                .filter(ct -> ct.type.equalsIgnoreCase(type))
                .findFirst()
                .orElseThrow(() -> new AppException(ErrorCode.INVALID_CALENDAR_TYPE, ErrorCode.INVALID_CALENDAR_TYPE.getMessage()));
    }
}