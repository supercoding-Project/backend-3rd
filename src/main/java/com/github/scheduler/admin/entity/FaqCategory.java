package com.github.scheduler.admin.entity;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum FaqCategory {

    NOTIFICATION("알림"),
    SCHEDULE("일정"),
    CALENDAR("캘린더"),
    USER("유저/계정"),
    INVITE_CODE("초대코드"),
    CHAT("채팅"),
    ETC("기타");

    private final String description;

}
