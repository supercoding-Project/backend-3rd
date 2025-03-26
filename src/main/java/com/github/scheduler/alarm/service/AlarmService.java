package com.github.scheduler.alarm.service;

import com.github.scheduler.alarm.dto.AlarmResponseDto;
import com.github.scheduler.alarm.dto.SchedulerAlarmDto;
import com.github.scheduler.alarm.entity.SchedulerAlarmEntity;
import com.github.scheduler.alarm.entity.SchedulerInvitationAlarmEntity;
import com.github.scheduler.alarm.repository.SchedulerAlarmRepository;
import com.github.scheduler.alarm.repository.SchedulerInvitationAlarmRepository;
import com.github.scheduler.auth.entity.UserEntity;
import com.github.scheduler.auth.repository.UserRepository;
import com.github.scheduler.calendar.entity.CalendarEntity;
import com.github.scheduler.calendar.entity.CalendarType;
import com.github.scheduler.calendar.entity.UserCalendarEntity;
import com.github.scheduler.global.config.alarm.WebSocketSessionManager;
import com.github.scheduler.global.exception.AppException;
import com.github.scheduler.global.exception.ErrorCode;
import com.github.scheduler.schedule.entity.RepeatType;
import com.github.scheduler.schedule.entity.ScheduleEntity;
import com.github.scheduler.schedule.repository.ScheduleRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
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

    // 읽음처리
    @Transactional
    public SchedulerAlarmDto markAlarmAsRead(Long userId, Long alarmId, String alarmType) {
        if ("schedule".equalsIgnoreCase(alarmType)) {
            SchedulerAlarmEntity alarm = schedulerAlarmRepository.findById(alarmId)
                    .orElseThrow(() -> new AppException(ErrorCode.NOT_FOUND_ALARM, ErrorCode.NOT_FOUND_ALARM.getMessage()));
            validateUserAccess(userId, alarm.getUser().getUserId());
            alarm.setChecked(true);
            schedulerAlarmRepository.save(alarm);
            return new SchedulerAlarmDto(alarm.getId(), alarm.getUser().getUserId(), alarm.getSchedule().getScheduleId(),alarm.getType(), alarm.isChecked());
        } else if ("invitation".equalsIgnoreCase(alarmType)) {
            SchedulerInvitationAlarmEntity invitationAlarm = schedulerInvitationAlarmRepository.findById(alarmId)
                    .orElseThrow(() -> new AppException(ErrorCode.NOT_FOUND_INVITE_ALARM, ErrorCode.NOT_FOUND_INVITE_ALARM.getMessage()));
            validateUserAccess(userId, invitationAlarm.getUser().getUserId());
            invitationAlarm.setChecked(true);
            schedulerInvitationAlarmRepository.save(invitationAlarm);
            return new SchedulerAlarmDto(invitationAlarm.getId(), invitationAlarm.getUser().getUserId(), null, invitationAlarm.getType(), invitationAlarm.isChecked());
        } else {
            throw new AppException(ErrorCode.CHECK_TYPE, ErrorCode.CHECK_TYPE.getMessage());
        }
    }

    private void validateUserAccess(Long requestUserId, Long alarmOwnerId) {
        if (!requestUserId.equals(alarmOwnerId)) {
            throw new AppException(ErrorCode.CANNOT_READ, ErrorCode.CANNOT_READ.getMessage());
        }
    }

    // 읽지않은 알림 전체 조회
    @Transactional
    public List<SchedulerAlarmDto> getUnreadAlarms(Long userId) {
        List<SchedulerAlarmEntity> unreadAlarms = schedulerAlarmRepository.findByUser_UserIdAndIsCheckedFalse(userId);
        List<SchedulerInvitationAlarmEntity> unreadInvitationAlarms = schedulerInvitationAlarmRepository.findByUser_UserIdAndIsCheckedFalse(userId);

        List<SchedulerAlarmDto> alarmDtos = new ArrayList<>();
        unreadAlarms.forEach(alarm ->
                alarmDtos.add(new SchedulerAlarmDto(
                        alarm.getId(), alarm.getUser().getUserId(), alarm.getSchedule() != null ? alarm.getSchedule().getScheduleId() : null,
                        alarm.getType(), alarm.isChecked()
                ))
        );
        unreadInvitationAlarms.forEach(inviteAlarm ->
                alarmDtos.add(new SchedulerAlarmDto(
                        inviteAlarm.getId(), inviteAlarm.getUser().getUserId(), null, inviteAlarm.getType(), inviteAlarm.isChecked()
                ))
        );
        return alarmDtos;
    }


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
        log.info("알림 전송: {} -> {}, {}", alarm.getType(), userEmail, alarmDto);
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

            SchedulerAlarmDto invitationAlarmDto = new SchedulerAlarmDto(
                    user.getUserId(),
                    calendar.getCalendarId(),
                    null,
                    type,
                    invitationAlarm.isChecked()
            );
            messagingTemplate.convertAndSendToUser(user.getEmail(), "/queue/alarms", invitationAlarmDto);
            log.info("초대 알림 저장 완료: id={} -> {}, {}", invitationAlarm.getId(), user.getEmail(), invitationAlarmDto);
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
            LocalDateTime now = LocalDateTime.now().withSecond(0).withNano(0);

            log.info("현재 시간: {}", now);

            List<ScheduleEntity> schedules = scheduleRepository.findAll();
            for (ScheduleEntity schedule : schedules) {
                if (!isScheduleMatching(schedule, now) || isDuplicateAlarm(schedule, now)) {
                    continue;
                }

                List<UserEntity> recipients = getRecipients(schedule);
                for (UserEntity recipient : recipients) {
                    // 사용자가 현재 접속 중이면 실시간 전송
                    if (onlineUserIds.contains(recipient.getUserId())) {
                        SchedulerAlarmEntity alarm = SchedulerAlarmEntity.builder()
                                .user(recipient)
                                .calendar(schedule.getCalendar())
                                .schedule(schedule)
                                .type("event_started")
                                .isChecked(false)
                                .createdAt(LocalDateTime.now())
                                .updatedAt(LocalDateTime.now())
                                .build();

                        schedulerAlarmRepository.save(alarm); // 알림 저장

                        sendAlarmToUser(recipient.getEmail(), alarm);
                    }else{
                        sendAlarm(recipient, schedule.getCalendar(), schedule, "event_started");
                    }
                }
            }
        } catch (Exception ex) {
            log.error("알림 전송 중 예기치 못한 오류 발생: {}", ex.getMessage(), ex);
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
