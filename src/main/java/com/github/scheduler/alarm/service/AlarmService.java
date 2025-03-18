package com.github.scheduler.alarm.service;

import com.github.scheduler.alarm.dto.SchedulerAlarmDto;
import com.github.scheduler.alarm.entity.SchedulerAlarmEntity;
import com.github.scheduler.alarm.repository.SchedulerAlarmRepository;
import com.github.scheduler.auth.entity.UserEntity;
import com.github.scheduler.auth.repository.UserRepository;
import com.github.scheduler.calendar.entity.CalendarEntity;
import com.github.scheduler.calendar.repository.UserCalendarRepository;
import com.github.scheduler.global.exception.AppException;
import com.github.scheduler.global.exception.ErrorCode;
import com.github.scheduler.schedule.dto.ScheduleDto;
import com.github.scheduler.schedule.entity.ScheduleStatus;
import com.github.scheduler.schedule.entity.SchedulerEntity;
import com.github.scheduler.schedule.repository.ScheduleRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class AlarmService {

    private final SchedulerAlarmRepository schedulerAlarmRepository;
    private final UserRepository userRepository;
    private final ScheduleRepository scheduleRepository;
    private final UserCalendarRepository userCalendarRepository;
    private final SimpMessagingTemplate messagingTemplate;

    public void sendAlarmToUser(String userEmail, SchedulerAlarmEntity alarm) {
        messagingTemplate.convertAndSendToUser(userEmail, "/queue/alarms", alarm);
        log.info("알림 전송: {} -> {}", alarm.getType(), userEmail);
    }

    @Transactional
    public void checkAndSendScheduleAlarms(Long userId) {
        UserEntity user = userRepository.findById(userId)
                .orElseThrow(() -> new AppException(ErrorCode.NOT_FOUND_USER, ErrorCode.NOT_FOUND_USER.getMessage()));

        LocalDateTime now = LocalDateTime.now();
        log.info("현재 시간: {}", now);

        List<SchedulerEntity> matchingSchedules = scheduleRepository.findByCreateUserIdAndStartTimeLessThanEqualAndEndTimeGreaterThanEqual(user, now, now);

        if (matchingSchedules.isEmpty()) {
            return;
        }
        List<SchedulerAlarmEntity> alarms = new ArrayList<>();
        for (SchedulerEntity schedule : matchingSchedules) {
            String eventType = determineEventType(schedule, user);

            SchedulerAlarmEntity alarm = SchedulerAlarmEntity.builder()
                    .user(user)
                    .calendar(schedule.getCalendarId())
                    .schedule(schedule)
                    .isChecked(false)
                    .type(eventType)
                    .createdAt(now)
                    .updatedAt(now)
                    .build();

            alarms.add(alarm);
            sendAlarmToUser(user.getEmail(), alarm);
        }

        schedulerAlarmRepository.saveAll(alarms);
    }

    private String determineEventType(SchedulerEntity schedule, UserEntity user) {
        if (schedule.getStartTime().isEqual(LocalDateTime.now())) {
            return "event_started";
        } else if (schedule.getScheduleStatus() == ScheduleStatus.CANCELLED) {
            return "event_deleted";
        } else {
            return "event_updated";
        }
    }


}
