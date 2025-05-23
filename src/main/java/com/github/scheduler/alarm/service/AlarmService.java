package com.github.scheduler.alarm.service;


import com.corundumstudio.socketio.AckRequest;
import com.corundumstudio.socketio.SocketIOClient;
import com.corundumstudio.socketio.SocketIOServer;
import com.github.scheduler.alarm.dto.ResponseAlarmDto;
import com.github.scheduler.alarm.dto.SchedulerAlarmDto;
import com.github.scheduler.alarm.entity.SchedulerAlarmEntity;
import com.github.scheduler.alarm.entity.SchedulerInvitationAlarmEntity;
import com.github.scheduler.alarm.event.AlarmCreatedEvent;
import com.github.scheduler.alarm.repository.SchedulerAlarmRepository;
import com.github.scheduler.alarm.repository.SchedulerInvitationAlarmRepository;
import com.github.scheduler.auth.entity.UserEntity;
import com.github.scheduler.auth.repository.UserRepository;
import com.github.scheduler.calendar.entity.CalendarEntity;
import com.github.scheduler.calendar.entity.CalendarType;
import com.github.scheduler.calendar.entity.UserCalendarEntity;
import com.github.scheduler.chat.event.ChatRoomJoinEvent;
import com.github.scheduler.global.exception.AppException;
import com.github.scheduler.global.exception.ErrorCode;
import com.github.scheduler.schedule.entity.RepeatType;
import com.github.scheduler.schedule.entity.ScheduleEntity;
import com.github.scheduler.schedule.entity.ScheduleMentionEntity;
import com.github.scheduler.schedule.repository.ScheduleRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.support.TransactionSynchronizationAdapter;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Set;

@Slf4j
@Service
public class AlarmService {

    private final SchedulerAlarmRepository schedulerAlarmRepository;
    private final SchedulerInvitationAlarmRepository schedulerInvitationAlarmRepository;  // 초대 알림 리포지토리
    private final ScheduleRepository scheduleRepository;
    private final SocketIOServer socketIOServer;
    private final ApplicationEventPublisher eventPublisher;

    public AlarmService(SchedulerAlarmRepository schedulerAlarmRepository
            , SchedulerInvitationAlarmRepository schedulerInvitationAlarmRepository
            , ScheduleRepository scheduleRepository
            ,@Qualifier("alarmSocketServer") SocketIOServer socketIOServer
            ,ApplicationEventPublisher eventPublisher) {
        this.schedulerAlarmRepository = schedulerAlarmRepository;
        this.schedulerInvitationAlarmRepository = schedulerInvitationAlarmRepository;
        this.scheduleRepository = scheduleRepository;
        this.socketIOServer = socketIOServer;
        this.eventPublisher = eventPublisher;
    }

    //읽지않은 알림 전체 갯수
    public Long alarmCount(Long userId) {
        long count = 0L;
        count += schedulerAlarmRepository.countByUser_UserIdAndIsCheckedFalse(userId) == null
                ? 0L : schedulerAlarmRepository.countByUser_UserIdAndIsCheckedFalse(userId);
        count += schedulerInvitationAlarmRepository.countByUser_UserIdAndIsCheckedFalse(userId) == null
                ? 0L : schedulerInvitationAlarmRepository.countByUser_UserIdAndIsCheckedFalse(userId);

        return count;
    }

    // 읽음처리
    @Transactional
    public ResponseAlarmDto markAlarmAsRead(Long userId, Long alarmId, String alarmType) {
        if ("schedule".equalsIgnoreCase(alarmType)) {
            SchedulerAlarmEntity alarm = schedulerAlarmRepository.findById(alarmId)
                    .orElseThrow(() -> new AppException(ErrorCode.NOT_FOUND_ALARM, ErrorCode.NOT_FOUND_ALARM.getMessage()));
            validateUserAccess(userId, alarm.getUser().getUserId());
            alarm.setChecked(true);
            schedulerAlarmRepository.save(alarm);
            int mentionCount = alarm.getSchedule().getMentions() != null
                    ? alarm.getSchedule().getMentions().size()
                    : 0;
            return new ResponseAlarmDto(alarm.getId(), alarm.getType(),  alarm.getCalendar().getCalendarName(), alarm.getSchedule().getTitle(), alarm.getSchedule().getLocation(), mentionCount, alarm.getCreatedAt(), alarm.isChecked());
        } else if ("invitation".equalsIgnoreCase(alarmType)) {
            SchedulerInvitationAlarmEntity invitationAlarm = schedulerInvitationAlarmRepository.findById(alarmId)
                    .orElseThrow(() -> new AppException(ErrorCode.NOT_FOUND_INVITE_ALARM, ErrorCode.NOT_FOUND_INVITE_ALARM.getMessage()));
            validateUserAccess(userId, invitationAlarm.getUser().getUserId());
            invitationAlarm.setChecked(true);
            schedulerInvitationAlarmRepository.save(invitationAlarm);
            return new ResponseAlarmDto(invitationAlarm.getId(), invitationAlarm.getType(),  invitationAlarm.getCalendar().getCalendarName(), null, null, 0, invitationAlarm.getCreatedAt(), invitationAlarm.isChecked());
        } else {
            throw new AppException(ErrorCode.CHECK_TYPE, ErrorCode.CHECK_TYPE.getMessage());
        }
    }

    @Transactional
    public List<ResponseAlarmDto> markAllAlarmsAsRead(Long userId) {
        List<ResponseAlarmDto> responseAlarms = new ArrayList<>();

        // 안 읽은 스케줄 알림 전부 가져와서 읽음 처리
        List<SchedulerAlarmEntity> scheduleAlarms = schedulerAlarmRepository.findByUser_UserIdAndIsCheckedFalse(userId);
        for (SchedulerAlarmEntity alarm : scheduleAlarms) {
            alarm.setChecked(true);
            schedulerAlarmRepository.save(alarm);
            responseAlarms.add(new ResponseAlarmDto(
                    alarm.getId(),
                    alarm.getType(),
                    alarm.getCalendar().getCalendarName(),
                    alarm.getSchedule().getTitle(),
                    alarm.getSchedule().getLocation(),
                    alarm.getSchedule().getMentions() != null ? alarm.getSchedule().getMentions().size() : 0,
                    alarm.getCreatedAt(),
                    alarm.isChecked()
            ));
        }

        // 안 읽은 초대 알림 전부 가져와서 읽음 처리
        List<SchedulerInvitationAlarmEntity> invitationAlarms = schedulerInvitationAlarmRepository.findByUser_UserIdAndIsCheckedFalse(userId);
        for (SchedulerInvitationAlarmEntity alarm : invitationAlarms) {
            alarm.setChecked(true);
            schedulerInvitationAlarmRepository.save(alarm);
            responseAlarms.add(new ResponseAlarmDto(
                    alarm.getId(),
                    alarm.getType(),
                    alarm.getCalendar().getCalendarName(),
                    null,
                    null,
                    0,
                    alarm.getCreatedAt(),
                    alarm.isChecked()
            ));
        }

        return responseAlarms;
    }



    private void validateUserAccess(Long requestUserId, Long alarmOwnerId) {
        if (!requestUserId.equals(alarmOwnerId)) {
            throw new AppException(ErrorCode.CANNOT_READ, ErrorCode.CANNOT_READ.getMessage());
        }
    }

    // 읽지않은 알림 전체 조회
    @Transactional
    public List<ResponseAlarmDto> getUnreadAlarms(Long userId) {
        List<SchedulerAlarmEntity> unreadAlarms = schedulerAlarmRepository.findByUser_UserIdAndIsCheckedFalse(userId);
        List<SchedulerInvitationAlarmEntity> unreadInvitationAlarms = schedulerInvitationAlarmRepository.findByUser_UserIdAndIsCheckedFalse(userId);

        List<ResponseAlarmDto> alarmDtos = new ArrayList<>();
        unreadAlarms.forEach(alarm ->
                {
                    int mentionCount = (alarm.getSchedule().getMentions() != null)
                            ? alarm.getSchedule().getMentions().size()
                            : 0;
                    alarmDtos.add(new ResponseAlarmDto(
                            alarm.getId(),
                            alarm.getType(),
                            alarm.getCalendar().getCalendarName(),
                            alarm.getSchedule().getTitle(),
                            alarm.getSchedule().getLocation(),
                            mentionCount,
                            alarm.getCreatedAt(),
                            alarm.isChecked()
                    ));
                }
        );
        unreadInvitationAlarms.forEach(inviteAlarm ->
                alarmDtos.add(new ResponseAlarmDto(
                        inviteAlarm.getId(),
                        inviteAlarm.getType(),
                        inviteAlarm.getCalendar().getCalendarName(),
                        null,
                        null,
                        0,
                        inviteAlarm.getCreatedAt(),
                        inviteAlarm.isChecked()
                ))
        );
        return alarmDtos;
    }


    @Transactional
    public void sendAlarmToUser(String userEmail, SchedulerAlarmEntity alarm) {
        List<ScheduleMentionEntity> mentions = alarm.getSchedule().getMentions();
        int mentionCount = (mentions == null || mentions.isEmpty()) ? 0 : mentions.size();
        ResponseAlarmDto alarmDto = new ResponseAlarmDto(
                alarm.getId(),
                alarm.getType(),
                alarm.getCalendar().getCalendarName(),
                alarm.getSchedule().getTitle(),
                alarm.getSchedule().getLocation(),
                mentionCount,
                alarm.getCreatedAt(),
                alarm.isChecked()
        );
        for (SocketIOClient client : socketIOServer.getAllClients()) {
            String connectedUserEmail = client.get("email");
            if (userEmail.equals(connectedUserEmail)) {
                sendAlarmToClient(client, alarmDto, null);  // 전용 핸들러 메서드 사용
            }
        }
    }

    public void sendAlarmToClient(SocketIOClient client, ResponseAlarmDto alarmDto, AckRequest ackRequest) {
        if (client != null && alarmDto != null) {
//            client.sendEvent("receiveAlarm", ackRequest, alarmDto);
            eventPublisher.publishEvent(new AlarmCreatedEvent(alarmDto,client));
            log.info("클라이언트 알림 전송 완료 -> {}, {}", client.get("email"), alarmDto);
        } else {
            log.warn("알림 전송 실패: client 또는 alarmDto가 null");
        }
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

            int memberCount = (calendar == null || calendar.getUserCalendars().isEmpty()) ? 0 : calendar.getUserCalendars().size();

            ResponseAlarmDto alarmDto = new ResponseAlarmDto(
                    invitationAlarm.getId(),
                    type,
                    Objects.requireNonNull(calendar).getCalendarName(),
                    null,
                    null,
                    memberCount,
                    invitationAlarm.getCreatedAt(),
                    invitationAlarm.isChecked()
            );
            //messagingTemplate.convertAndSendToUser(user.getEmail(), "/queue/alarms", invitationAlarmDto);
            for (SocketIOClient client : socketIOServer.getAllClients()) {
                String connectedUserEmail = client.get("email");
                if (user.getEmail().equals(connectedUserEmail)) {
                    TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronizationAdapter() {
                        @Override
                        public void afterCommit() {
                            sendAlarmToClient(client, alarmDto, null);  // 전용 핸들러 메서드 사용
                        }
                    });
                }
            }
            log.info("초대 알림 저장 완료: id={} -> {}, {}", invitationAlarm.getId(), user.getEmail(), alarmDto);
        } catch (Exception e) {
            log.error("초대 알림 저장 실패: {}", e.getMessage());
            throw new RuntimeException("초대 알림 저장 실패", e);  // RuntimeException으로 롤백 트리거
        }
    }

    @Transactional
    @Scheduled(cron = "0 * * * * *") // 매분마다 실행
    public void checkAndSendScheduleAlarms() {
        try {
           // Set<Long> onlineUserIds = sessionManager.getConnectedUsers(); // 접속된 사용자들
            LocalDateTime now = LocalDateTime.now().withSecond(0).withNano(0);
            LocalDateTime oneMinuteBefore = now.minusMinutes(1);
            LocalDateTime oneMinuteAfter = now.plusMinutes(1);
            log.info("현재 시간: {}", now);

            //List<ScheduleEntity> schedules = scheduleRepository.findAll();

            List<ScheduleEntity> oneTimeSchedules =
                    scheduleRepository.findByRepeatTypeAndStartTimeBetween(RepeatType.NONE, oneMinuteBefore, oneMinuteAfter);

            List<ScheduleEntity> repeatingSchedules =
                    scheduleRepository.findByRepeatTypeNotAndRepeatEndDateGreaterThanEqual(RepeatType.NONE, now.toLocalDate());

            List<ScheduleEntity> allToNotify = new ArrayList<>();

            allToNotify.addAll(oneTimeSchedules);

            for (ScheduleEntity schedule : repeatingSchedules) {
                if (isScheduleMatching(schedule, now) && !isDuplicateAlarm(schedule, now)) {
                    allToNotify.add(schedule);
                }
            }

            // 알림 전송
            for (ScheduleEntity schedule : allToNotify) {
                List<UserEntity> recipients = getRecipients(schedule);
                for (UserEntity user : recipients) {
                    SchedulerAlarmEntity alarm = SchedulerAlarmEntity.builder()
                            .user(user)
                            .calendar(schedule.getCalendar())
                            .schedule(schedule)
                            .type("event_started")
                            .isChecked(false)
                            .createdAt(LocalDateTime.now())
                            .updatedAt(LocalDateTime.now())
                            .build();

                    schedulerAlarmRepository.save(alarm);
                    TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronizationAdapter() {
                        @Override
                        public void afterCommit() {
                            sendAlarmToUser(user.getEmail(), alarm);
                        }
                    });
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

        int interval = schedule.getRepeatInterval();
        if (interval <= 0) {
            interval = 1;
        }

        switch (schedule.getRepeatType()) {
            case DAILY:
                return ChronoUnit.DAYS.between(startTime.toLocalDate(), now.toLocalDate()) % interval == 0
                        && startTime.getHour() == now.getHour()
                        && startTime.getMinute() == now.getMinute();

            case WEEKLY:
                return ChronoUnit.WEEKS.between(startTime.toLocalDate(), now.toLocalDate()) % interval == 0
                        && startTime.getDayOfWeek() == now.getDayOfWeek()
                        && startTime.getHour() == now.getHour()
                        && startTime.getMinute() == now.getMinute();

            case MONTHLY:
                return ChronoUnit.MONTHS.between(startTime.toLocalDate(), now.toLocalDate()) % interval == 0
                        && startTime.getDayOfMonth() == now.getDayOfMonth()
                        && startTime.getHour() == now.getHour()
                        && startTime.getMinute() == now.getMinute();

            case YEARLY:
                return ChronoUnit.YEARS.between(startTime.toLocalDate(), now.toLocalDate()) % interval == 0
                        && startTime.getMonth() == now.getMonth()
                        && startTime.getDayOfMonth() == now.getDayOfMonth()
                        && startTime.getHour() == now.getHour()
                        && startTime.getMinute() == now.getMinute();

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
        TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronizationAdapter() {
            @Override
            public void afterCommit() {
                sendAlarmToUser(user.getEmail(), alarm);
            }
        });
    }

}
