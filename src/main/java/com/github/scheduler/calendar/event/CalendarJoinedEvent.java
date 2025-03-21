package com.github.scheduler.calendar.event;

import com.github.scheduler.auth.entity.UserEntity;
import com.github.scheduler.calendar.entity.CalendarEntity;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public class CalendarJoinedEvent {
    private final CalendarEntity calendar;
    private final UserEntity newUser;
}
