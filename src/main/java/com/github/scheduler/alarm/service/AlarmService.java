package com.github.scheduler.alarm.service;

import com.github.scheduler.alarm.dto.SchedulerAlarmDto;
import com.github.scheduler.alarm.dto.SchedulerInvitationAlarmDto;
import com.github.scheduler.alarm.entity.AlarmType;
import com.github.scheduler.alarm.entity.SchedulerAlarmEntity;
import com.github.scheduler.alarm.entity.SchedulerInvitationAlarmEntity;
import com.github.scheduler.alarm.repository.SchedulerAlarmRepository;
import com.github.scheduler.alarm.repository.SchedulerInvitationAlarmRepository;
import com.github.scheduler.auth.entity.UserEntity;
import com.github.scheduler.auth.repository.UserRepository;
import com.github.scheduler.calendar.entity.CalendarEntity;
import com.github.scheduler.calendar.entity.CalendarType;
import com.github.scheduler.calendar.entity.UserCalendarEntity;
import com.github.scheduler.calendar.repository.UserCalendarRepository;
import com.github.scheduler.global.config.alarm.SessionManager;
import com.github.scheduler.global.config.alarm.WebSocketSessionManager;
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
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;

@Slf4j
@Service
@RequiredArgsConstructor
public class AlarmService {

    private final SchedulerAlarmRepository schedulerAlarmRepository;
    private final SchedulerInvitationAlarmRepository schedulerInvitationAlarmRepository;  // 초대 알림 리포지토리
    private final UserRepository userRepository;
    private final ScheduleRepository scheduleRepository;
    @Autowired
    private final SimpMessagingTemplate messagingTemplate;
    private final WebSocketSessionManager sessionManager;

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


    // 초대 알림 전송
    @Transactional
    public void sendInvitationAlarms(CalendarEntity calendar, UserEntity invitedUser) {
        List<UserEntity> members = calendar.getUserCalendars().stream()
                .map(UserCalendarEntity::getUserEntity)
                .toList();

        // 초대된 사용자 제외하고 초대 알림
        for (UserEntity member : members) {
            if (!member.equals(invitedUser)) {
                sendInvitationAlarm(member, calendar, "member_added");
            }
        }
        // 초대받은 사용자에게 초대 알림
        sendInvitationAlarm(invitedUser, calendar, "member_invited");
    }

    // 초대 알림 생성 및 전송
    @Transactional
    public void sendInvitationAlarm(UserEntity user, CalendarEntity calendar, String type) {
        try {
            SchedulerInvitationAlarmEntity invitationAlarm = SchedulerInvitationAlarmEntity.builder()
                    .user(user)
                    .calendar(calendar)
                    .type(type)
                    .isChecked(false)
                    .createdAt(LocalDateTime.now())
                    .updatedAt(LocalDateTime.now())
                    .build();
            schedulerInvitationAlarmRepository.save(invitationAlarm);

            // 클라이언트에 알림 전송
            SchedulerInvitationAlarmDto invitationAlarmDto = new SchedulerInvitationAlarmDto(
                    user.getUserId(),
                    calendar.getCalendarId(),
                    type,
                    invitationAlarm.isChecked()
            );
            messagingTemplate.convertAndSendToUser(user.getEmail(), "/queue/alarms", invitationAlarmDto);
            log.info("초대 알림 저장 완료: id={} -> {}", invitationAlarm.getId(), user.getEmail());
        } catch (Exception e) {
            log.error("초대 알림 저장 실패: {}", e.getMessage());
            throw new RuntimeException("초대 알림 저장 실패", e);  // RuntimeException으로 롤백 트리거
        }
    }

    @Transactional
    @Scheduled(cron = "0 * * * * *") // 매분마다 실행
    public void checkAndSendScheduleAlarms() {
        try {
            Set<Long> onlineUserIds = sessionManager.getConnectedUsers(); // 접속된 사용자들

            if (onlineUserIds.isEmpty()) {
                log.info("🔕 현재 접속 중인 사용자가 없습니다.");
                return;
            }
            // 각 사용자에 대해 스케줄 알림을 처리
            processScheduleAlarms(onlineUserIds);
        } catch (Exception ex) {
            log.error("알림 전송 중 예기치 못한 오류 발생: {}", ex.getMessage(), ex);
        }
    }

    private void processScheduleAlarms(Set<Long> onlineUserIds) {
        LocalDateTime now = LocalDateTime.now().withSecond(0).withNano(0);
        log.info("📅 현재 시간: {}", now);

        for (Long userId : onlineUserIds) {
            UserEntity user = userRepository.findById(userId)
                    .orElseThrow(() -> new AppException(ErrorCode.NOT_FOUND_USER, ErrorCode.NOT_FOUND_USER.getMessage()));

            List<CalendarEntity> calendars = user.getUserCalendars().stream()
                    .map(UserCalendarEntity::getCalendarEntity)
                    .toList();

            for (CalendarEntity calendar : calendars) {
                List<ScheduleEntity> schedules = scheduleRepository.findByCalendar(calendar);

                for (ScheduleEntity schedule : schedules) {
                    if (!isScheduleMatching(schedule, now) || isDuplicateAlarm(schedule, now)) {
                        continue;
                    }

                    List<UserEntity> recipients = getRecipients(schedule);
                    for (UserEntity recipient : recipients) {
                        if (onlineUserIds.contains(recipient.getUserId())) {
                            sendAlarm(recipient, schedule.getCalendar(), schedule, "event_started");
                        }
                    }
                }
            }
        }
    }

    private boolean isScheduleMatching(ScheduleEntity schedule, LocalDateTime now) {
        LocalDateTime startTime = schedule.getStartTime().withSecond(0).withNano(0);
        if (schedule.getRepeatType() == RepeatType.NONE) {
            return startTime.equals(now);
        }
        if (schedule.getRepeatEndDate() != null && now.toLocalDate().isAfter(schedule.getRepeatEndDate())) {
            return false;
        }
        switch (schedule.getRepeatType()) {
            case DAILY:
                return startTime.getHour() == now.getHour() && startTime.getMinute() == now.getMinute();
            case WEEKLY:
                return startTime.getDayOfWeek() == now.getDayOfWeek() && startTime.getHour() == now.getHour() && startTime.getMinute() == now.getMinute();
            case MONTHLY:
                return startTime.getDayOfMonth() == now.getDayOfMonth() && startTime.getHour() == now.getHour() && startTime.getMinute() == now.getMinute();
            case YEARLY:
                return startTime.getMonth() == now.getMonth() && startTime.getDayOfMonth() == now.getDayOfMonth() && startTime.getHour() == now.getHour() && startTime.getMinute() == now.getMinute();
            default:
                return false;
        }
    }

    private boolean isDuplicateAlarm(ScheduleEntity schedule, LocalDateTime now) {
        return schedulerAlarmRepository.existsByScheduleAndTypeAndCreatedAtAfter(schedule, "event_started", now.minusMinutes(1));
    }

    private List<UserEntity> getRecipients(ScheduleEntity schedule) {
        if (schedule.getCalendar().getCalendarType() == CalendarType.SHARED) {
            return schedule.getCalendar().getUserCalendars().stream()
                    .map(UserCalendarEntity::getUserEntity)
                    .toList();
        } else {
            return List.of(schedule.getCreateUserId());
        }
    }

    @Transactional
    public void sendAlarm(UserEntity user, CalendarEntity calendar, ScheduleEntity schedule, String type) {
        SchedulerAlarmEntity alarm = SchedulerAlarmEntity.builder()
                .user(user)
                .calendar(calendar)
                .schedule(schedule)
                .isChecked(false)
                .type(type)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
        schedulerAlarmRepository.save(alarm);
        sendAlarmToUser(user.getEmail(), alarm);
    }
}
