package com.github.scheduler.alarm.service;

import com.github.scheduler.alarm.dto.SchedulerAlarmDto;
import com.github.scheduler.alarm.entity.AlarmType;
import com.github.scheduler.alarm.entity.SchedulerAlarmEntity;
import com.github.scheduler.alarm.repository.SchedulerAlarmRepository;
import com.github.scheduler.auth.entity.UserEntity;
import com.github.scheduler.auth.repository.UserRepository;
import com.github.scheduler.calendar.entity.CalendarEntity;
import com.github.scheduler.calendar.repository.UserCalendarRepository;
import com.github.scheduler.global.config.auth.custom.CustomUserDetails;
import com.github.scheduler.global.exception.AppException;
import com.github.scheduler.global.exception.ErrorCode;
import com.github.scheduler.schedule.dto.ScheduleDto;
import com.github.scheduler.schedule.entity.RepeatType;
import com.github.scheduler.schedule.entity.ScheduleEntity;
import com.github.scheduler.schedule.entity.ScheduleStatus;
import com.github.scheduler.schedule.repository.ScheduleRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class AlarmService {

    private final SchedulerAlarmRepository schedulerAlarmRepository;
    private final UserRepository userRepository;
    private final ScheduleRepository scheduleRepository;
    private final SimpMessagingTemplate messagingTemplate;

    @Transactional
    public void sendAlarmToUser(String userEmail, SchedulerAlarmEntity alarm) {
        SchedulerAlarmDto alarmDto = new SchedulerAlarmDto(
                alarm.getUser().getUserId(),
                alarm.getCalendar().getCalendarId(),
                alarm.getSchedule().getScheduleId(),
                alarm.getType(),
                alarm.isChecked()
        );
        messagingTemplate.convertAndSendToUser(userEmail, "/queue/alarms", alarmDto);
        log.info("알림 전송: {} -> {}", alarm.getType(), userEmail);
    }

    @Transactional
    @Scheduled(cron = "0 * * * * *")
    public void checkAndSendScheduleAlarms() {
        try {
            Long userId = 1L;  // 테스트
//        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
//
//        if (authentication == null || !authentication.isAuthenticated() || authentication.getPrincipal().equals("anonymousUser")) {
//            log.warn("인증된 사용자가 없음.");
//            return;
//        }
//
//        CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
//        Long userId = userDetails.getUserEntity().getUserId();

            UserEntity user = userRepository.findById(userId)
                    .orElseThrow(() -> new AppException(ErrorCode.NOT_FOUND_USER, ErrorCode.NOT_FOUND_USER.getMessage()));

            LocalDateTime now = LocalDateTime.now().withSecond(0).withNano(0);
            log.info("현재 시간: {}", now);

            List<ScheduleEntity> schedules = scheduleRepository.findByCreateUserId(user);

            for (ScheduleEntity schedule : schedules) {
                if (!isScheduleMatching(schedule, now)) {
                    continue;
                }

                String eventType = determineEventType(schedule);

                SchedulerAlarmEntity alarm = SchedulerAlarmEntity.builder()
                        .user(user)
                        .calendar(schedule.getCalendar())
                        .schedule(schedule)
                        .isChecked(false)
                        .type(eventType)
                        .createdAt(now)
                        .updatedAt(now)
                        .build();

                schedulerAlarmRepository.save(alarm);
                sendAlarmToUser(user.getEmail(), alarm);
            }
        }  catch (AppException ex) {
            log.error("❌ 알림전송 실패: {}", ex.getMessage());
        }
    }

    private boolean isScheduleMatching(ScheduleEntity schedule, LocalDateTime now) {
        LocalDateTime startTime = schedule.getStartTime().withSecond(0).withNano(0);

        if (schedule.getRepeatType() == RepeatType.NONE) {
            return startTime.equals(now);
        }

        // 반복 종료일 체크
        if (schedule.getRepeatEndDate() != null && now.toLocalDate().isAfter(schedule.getRepeatEndDate())) {
            return false;
        }

        switch (schedule.getRepeatType()) {
            case DAILY:
                return startTime.getHour() == now.getHour() &&
                        startTime.getMinute() == now.getMinute();

            case WEEKLY:
                return startTime.getDayOfWeek() == now.getDayOfWeek() &&
                        startTime.getHour() == now.getHour() &&
                        startTime.getMinute() == now.getMinute() &&
                        ChronoUnit.WEEKS.between(startTime.toLocalDate(), now.toLocalDate()) % schedule.getRepeatInterval() == 0;

            case MONTHLY:
                return startTime.getDayOfMonth() == now.getDayOfMonth() &&
                        startTime.getHour() == now.getHour() &&
                        startTime.getMinute() == now.getMinute() &&
                        ChronoUnit.MONTHS.between(startTime.toLocalDate(), now.toLocalDate()) % schedule.getRepeatInterval() == 0;

            case YEARLY:
                return startTime.getMonth() == now.getMonth() &&
                        startTime.getDayOfMonth() == now.getDayOfMonth() &&
                        startTime.getHour() == now.getHour() &&
                        startTime.getMinute() == now.getMinute() &&
                        ChronoUnit.YEARS.between(startTime.toLocalDate(), now.toLocalDate()) % schedule.getRepeatInterval() == 0;

            default:
                return false;
        }
    }


    private String determineEventType(ScheduleEntity schedule) {
        if (schedule.getStartTime().isEqual(LocalDateTime.now())) {
            return "event_started";
        } else if (schedule.getScheduleStatus() == ScheduleStatus.CANCELLED) {
            return "event_deleted";
        } else {
            return "event_updated";
        }
    }
}
