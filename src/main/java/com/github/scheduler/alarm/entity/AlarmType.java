package com.github.scheduler.alarm.entity;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum AlarmType {
    EVENT_ADDED("event_added"),
    EVENT_MENTIONED("event_mentioned"),
    EVENT_DELETED("event_deleted"),
    EVENT_UPDATED("event_updated"),
    MEMBER_ADDED("member_added"),
    MEMBER_INVITED("member_invited"),
    EVENT_STARTED("event_started");

    private final String type;
}
