package com.github.scheduler.alarm.event;

import com.corundumstudio.socketio.SocketIOClient;
import com.github.scheduler.alarm.dto.ResponseAlarmDto;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public class AlarmCreatedEvent {
    private final ResponseAlarmDto alarmData;
    private final SocketIOClient socketIOClient;
}