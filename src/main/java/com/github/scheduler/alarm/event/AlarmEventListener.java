package com.github.scheduler.alarm.event;

import com.github.scheduler.alarm.dto.ScheduleEvent;
import com.github.scheduler.alarm.dto.SchedulerAlarmDto;
import com.github.scheduler.alarm.service.AlarmService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import java.util.Set;

@Slf4j
@Component
@RequiredArgsConstructor
public class AlarmEventListener {
    private final AlarmService alarmService;

    @EventListener
    public void handleScheduleEvent(ScheduleEvent event) {
        alarmService.checkAndSendScheduleAlarms();
    }
}
