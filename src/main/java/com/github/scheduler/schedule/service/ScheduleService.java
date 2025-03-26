package com.github.scheduler.schedule.service;

import com.github.scheduler.alarm.service.AlarmService;
import com.github.scheduler.auth.entity.UserEntity;
import com.github.scheduler.auth.repository.UserRepository;
import com.github.scheduler.calendar.entity.CalendarEntity;
import com.github.scheduler.calendar.entity.CalendarType;
import com.github.scheduler.calendar.entity.UserCalendarEntity;
import com.github.scheduler.calendar.repository.CalendarRepository;
import com.github.scheduler.calendar.repository.UserCalendarRepository;
import com.github.scheduler.global.config.auth.custom.CustomUserDetails;
import com.github.scheduler.global.exception.AppException;
import com.github.scheduler.global.exception.ErrorCode;
import com.github.scheduler.schedule.dto.*;
import com.github.scheduler.schedule.entity.RepeatType;
import com.github.scheduler.schedule.entity.ScheduleEntity;
import com.github.scheduler.schedule.entity.ScheduleMentionEntity;
import com.github.scheduler.schedule.entity.ScheduleStatus;
import com.github.scheduler.schedule.event.DeleteScheduleEvent;
import com.github.scheduler.schedule.event.ScheduleCreatedEvent;
import com.github.scheduler.schedule.event.UpdateScheduleEvent;
import com.github.scheduler.schedule.repository.ScheduleMentionRepository;
import com.github.scheduler.schedule.repository.ScheduleRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.*;
import java.time.temporal.TemporalAdjusters;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class ScheduleService {

    private final CalendarRepository calendarRepository;
    private final UserCalendarRepository userCalendarRepository;
    private final ScheduleRepository scheduleRepository;
    private final ScheduleMentionRepository scheduleMentionRepository;
    private final UserRepository userRepository;
    private final ApplicationEventPublisher eventPublisher;

    private final AlarmService alarmService;

    //일정 조회(monthly,weekly,daily)
    @Transactional
    public List<ScheduleDto> getSchedules(CustomUserDetails customUserDetails, String view, String date, List<Long> calendarId) {

        // 인증된 사용자 확인
        if (customUserDetails == null) {
            throw new AppException(ErrorCode.NOT_FOUND_USER, ErrorCode.NOT_FOUND_USER.getMessage());
        }

        // 날짜 파싱 및 조회 범위 계산
        LocalDate targetDate = LocalDate.parse(date);
        LocalDateTime startDateTime;
        LocalDateTime endDateTime;

        if (view.equalsIgnoreCase("MONTHLY")) {
            // 월별 조회: 기준 날짜의 첫 날부터 마지막 날까지
            startDateTime = targetDate.withDayOfMonth(1).atStartOfDay();
            endDateTime = targetDate.withDayOfMonth(targetDate.lengthOfMonth()).atTime(LocalTime.MAX);
        } else if (view.equalsIgnoreCase("WEEKLY")) {
            // 주별 조회: 해당 주의 월요일부터 일요일까지
            LocalDate monday = targetDate.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));
            LocalDate sunday = targetDate.with(TemporalAdjusters.nextOrSame(DayOfWeek.SUNDAY));
            startDateTime = monday.atStartOfDay();
            endDateTime = sunday.atTime(LocalTime.MAX);
        } else if (view.equalsIgnoreCase("DAILY")) {
            // 일별 조회: 해당 날짜의 시작과 끝
            startDateTime = targetDate.atStartOfDay();
            endDateTime = targetDate.atTime(LocalTime.MAX);
        } else {
            throw new AppException(ErrorCode.DATE_FORMAT_INCORRECT, ErrorCode.DATE_FORMAT_INCORRECT.getMessage());
        }

        List<ScheduleEntity> scheduleEntities = scheduleRepository.findByCalendarCalendarIdInAndScheduleStatusNotAndStartTimeBetween(calendarId, ScheduleStatus.CANCELLED, startDateTime, endDateTime);
        List<ScheduleDto> result = new ArrayList<>();
        for (ScheduleEntity schedule : scheduleEntities) {
            ScheduleDto dto = convertScheduleEntityToDto(schedule);
            // 공유 캘린더인 경우, 캘린더에 소속된 userId 목록만 설정
            if (schedule.getCalendar() != null && schedule.getCalendar().getCalendarType().equals(CalendarType.SHARED)) {
                List<Long> sharedIds = userCalendarRepository
                        .findByCalendarEntityCalendarId(schedule.getCalendar().getCalendarId())
                        .stream()
                        .map(userCalendarEntity -> userCalendarEntity.getUserEntity().getUserId())
                        .toList();
                dto.setSharedUserIds(sharedIds);
            }
            result.add(dto);
        }

        // 여러 타입이 선택된 경우, 시작 시간 기준 정렬
        result.sort(Comparator.comparing(ScheduleDto::getStartTime));
        return result;
    }

    // ScheduleEntity -> ScheduleDto 변환
    private ScheduleDto convertScheduleEntityToDto(ScheduleEntity entity) {
        RepeatScheduleDto repeatScheduleDto = RepeatScheduleDto.builder()
                .repeatType(String.valueOf(entity.getRepeatType()))
                .repeatInterval(entity.getRepeatInterval())
                .repeatEndDate(entity.getRepeatEndDate())
                .build();

        return ScheduleDto.builder()
                .scheduleId(entity.getScheduleId())
                .createUserId(entity.getCreateUserId().getUserId())
                .title(entity.getTitle())
                .startTime(entity.getStartTime())
                .endTime(entity.getEndTime())
                .repeatSchedule(repeatScheduleDto)
                .location(entity.getLocation())
                .memo(entity.getMemo())
                .calendarId(entity.getCalendar() != null ? entity.getCalendar().getCalendarId().toString() : null)
                .status(entity.getScheduleStatus().getScheduleStatus())
                .build();
    }

    //일정 등록
    @Transactional
    public List<CreateScheduleDto> createSchedule(CustomUserDetails customUserDetails, CreateScheduleDto createScheduleDto, Long calendarId) {

        if (customUserDetails == null) {
            throw new AppException(ErrorCode.NOT_FOUND_USER, ErrorCode.NOT_FOUND_USER.getMessage());
        }

        if (calendarId == null) {
            throw new AppException(ErrorCode.NOT_FOUND_CALENDAR, ErrorCode.NOT_FOUND_CALENDAR.getMessage());
        }

        // 전달받은 캘린더 ID를 이용해 캘린더 조회
        CalendarEntity calendarEntity = calendarRepository.findById(calendarId)
                .orElseThrow(() -> new AppException(ErrorCode.NOT_FOUND_CALENDAR, ErrorCode.NOT_FOUND_CALENDAR.getMessage()));

        // 캘린더 타입은 PERSONAL 또는 SHARED 여야 함
        if (!(calendarEntity.getCalendarType().equals(CalendarType.PERSONAL) || calendarEntity.getCalendarType().equals(CalendarType.SHARED))) {
            throw new AppException(ErrorCode.TODO_NOT_SUPPORTED, ErrorCode.TODO_NOT_SUPPORTED.getMessage());
        }

        //공유 캘린더의 경우에 사용자 맨션 시 캘린더에 포함되어있는 멤버인지 확인
        if (calendarEntity.getCalendarType().equals(CalendarType.SHARED)) {
            List<Long> calendarMemberIds = userCalendarRepository.findByCalendarEntityCalendarId(calendarEntity.getCalendarId())
                    .stream()
                    .map(userCalendarEntity -> userCalendarEntity.getUserEntity().getUserId())
                    .toList();
            if (createScheduleDto.getMentionUserIds() != null && !createScheduleDto.getMentionUserIds().isEmpty()) {
                for (Long mentionUserId : createScheduleDto.getMentionUserIds()) {
                    if (!calendarMemberIds.contains(mentionUserId)) {
                        throw new AppException(ErrorCode.INVALID_MENTION_USER, ErrorCode.INVALID_MENTION_USER.getMessage());
                    }
                }
            }
        }

        // 반복 설정 처리
        RepeatType repeatType = RepeatType.NONE;
        int repeatInterval = 0;
        LocalDate repeatEndDate = null;
        if (createScheduleDto.getRepeatSchedule() != null &&
                createScheduleDto.getRepeatSchedule().getRepeatType() != null &&
                !createScheduleDto.getRepeatSchedule().getRepeatType().isEmpty()) {
            repeatType = RepeatType.valueOf(createScheduleDto.getRepeatSchedule().getRepeatType().toUpperCase());
            repeatInterval = createScheduleDto.getRepeatSchedule().getRepeatInterval();
            repeatEndDate = createScheduleDto.getRepeatSchedule().getRepeatEndDate();
        }

        // 신규 일정 엔티티 생성
        ScheduleEntity scheduleEntity = ScheduleEntity.builder()
                .createUserId(customUserDetails.getUserEntity())
                .title(createScheduleDto.getTitle())
                .location(createScheduleDto.getLocation())
                .startTime(createScheduleDto.getStartTime())
                .endTime(createScheduleDto.getEndTime())
                .repeatType(repeatType)
                .repeatInterval(repeatInterval)
                .repeatEndDate(repeatEndDate)
                .memo(createScheduleDto.getMemo())
                .scheduleStatus(ScheduleStatus.SCHEDULED)
                .calendar(calendarEntity)
                .build();

        ScheduleEntity savedScheduleEntity = scheduleRepository.save(scheduleEntity);

        if (createScheduleDto.getMentionUserIds() != null && !createScheduleDto.getMentionUserIds().isEmpty()) {
            for (Long mentionUserId : createScheduleDto.getMentionUserIds()) {
                UserEntity mentionUser = userRepository.findById(mentionUserId)
                        .orElseThrow(() -> new AppException(ErrorCode.INVALID_MENTION_USER, ErrorCode.INVALID_MENTION_USER.getMessage()));
                ScheduleMentionEntity scheduleMentionEntity = ScheduleMentionEntity.builder()
                        .schedule(savedScheduleEntity)
                        .user(mentionUser)
                        .build();
                scheduleMentionRepository.save(scheduleMentionEntity);
            }
        }

        eventPublisher.publishEvent(new ScheduleCreatedEvent(scheduleEntity.getScheduleId(), "일정을 성공적으로 등록했습니다."));

        List<UserEntity> allMembers = Optional.ofNullable(savedScheduleEntity.getCalendar().getUserCalendars())
                .orElse(Collections.emptyList())
                .stream()
                .map(UserCalendarEntity::getUserEntity)
                .collect(Collectors.toList());

        // createScheduleDto에 포함된 멤버의 알림을 처리
        if (createScheduleDto.getMentionUserIds() != null && !createScheduleDto.getMentionUserIds().isEmpty()) {
            List<Long> mentionedUserIds = createScheduleDto.getMentionUserIds();  // mentionUserIds를 리스트로 가져옴

            // mentionUserIds에 포함된 유저들에게 'event_mentioned' 알림 전송
            for (Long mentionUserId : mentionedUserIds) {
                UserEntity mentionUser = userRepository.findById(mentionUserId)
                        .orElseThrow(() -> new AppException(ErrorCode.INVALID_MENTION_USER, ErrorCode.INVALID_MENTION_USER.getMessage()));
                alarmService.sendAlarm(mentionUser, savedScheduleEntity.getCalendar(), savedScheduleEntity, "event_mentioned");
            }
        }

        for (UserEntity member : allMembers) {
            // 해당 멤버가 mentionedUserIds에 포함되지 않으면 'event_added' 알림 전송
            if (!createScheduleDto.getMentionUserIds().contains(member.getUserId())) {
                alarmService.sendAlarm(member, savedScheduleEntity.getCalendar(), savedScheduleEntity, "event_added");
            }
        }

        CreateScheduleDto saveCreateScheduleDto = convertScheduleEntityToCreateScheduleDto(savedScheduleEntity);
        return Collections.singletonList(saveCreateScheduleDto);
    }

    private CreateScheduleDto convertScheduleEntityToCreateScheduleDto(ScheduleEntity saveScheduleEntity) {
        RepeatScheduleDto repeatScheduleDto = RepeatScheduleDto.builder()
                .repeatType(saveScheduleEntity.getRepeatType().name())
                .repeatInterval(saveScheduleEntity.getRepeatInterval())
                .repeatEndDate(saveScheduleEntity.getRepeatEndDate())
                .build();

        return CreateScheduleDto.builder()
                .createUserId(saveScheduleEntity.getCreateUserId().getUserId())
                .title(saveScheduleEntity.getTitle())
                .location(saveScheduleEntity.getLocation())
                .startTime(saveScheduleEntity.getStartTime())
                .endTime(saveScheduleEntity.getEndTime())
                .repeatSchedule(repeatScheduleDto)
                .memo(saveScheduleEntity.getMemo())
                .build();
    }

    //일정 수정
    @Transactional
    public List<UpdateScheduleDto> updateSchedule(CustomUserDetails customUserDetails, UpdateScheduleDto updateScheduleDto, Long scheduleId, Long calendarId) {

        if (customUserDetails == null) {
            throw new AppException(ErrorCode.NOT_FOUND_USER, ErrorCode.NOT_FOUND_USER.getMessage());
        }

        ScheduleEntity scheduleEntity = scheduleRepository.findById(scheduleId)
                .orElseThrow(() -> new AppException(ErrorCode.SCHEDULE_NOT_FOUND, ErrorCode.SCHEDULE_NOT_FOUND.getMessage()));

        CalendarEntity calendarEntity = scheduleEntity.getCalendar();
        if (calendarEntity == null || !calendarEntity.getCalendarId().equals(calendarId)) {
            throw new AppException(ErrorCode.INVALID_CALENDAR_ID, ErrorCode.INVALID_CALENDAR_ID.getMessage());
        }

        Long currentUserId = customUserDetails.getUserEntity().getUserId();

        if (calendarEntity.getCalendarType().equals(CalendarType.PERSONAL)) {
            if (!scheduleEntity.getCreateUserId().getUserId().equals(currentUserId)) {
                throw new AppException(ErrorCode.UNAUTHORIZED_ACCESS, ErrorCode.UNAUTHORIZED_ACCESS.getMessage());
            }
        } else {
            if (!userCalendarRepository.existsByCalendarEntityCalendarIdAndUserEntityUserId(
                    calendarEntity.getCalendarId(), currentUserId)) {
                throw new AppException(ErrorCode.UNAUTHORIZED_ACCESS, ErrorCode.UNAUTHORIZED_ACCESS.getMessage());
            }
        }

        // 수정할 필드 업데이트
        scheduleEntity.setTitle(updateScheduleDto.getTitle());
        scheduleEntity.setLocation(updateScheduleDto.getLocation());
        scheduleEntity.setStartTime(updateScheduleDto.getStartTime());
        scheduleEntity.setEndTime(updateScheduleDto.getEndTime());
        scheduleEntity.setMemo(updateScheduleDto.getMemo());
        // 반복 설정 수정
        if (updateScheduleDto.getRepeatSchedule() != null &&
                updateScheduleDto.getRepeatSchedule().getRepeatType() != null &&
                !updateScheduleDto.getRepeatSchedule().getRepeatType().isEmpty()) {
            RepeatType newRepeatType = RepeatType.valueOf(updateScheduleDto.getRepeatSchedule().getRepeatType().toUpperCase());
            scheduleEntity.setRepeatType(newRepeatType);
            scheduleEntity.setRepeatInterval(updateScheduleDto.getRepeatSchedule().getRepeatInterval());
            scheduleEntity.setRepeatEndDate(updateScheduleDto.getRepeatSchedule().getRepeatEndDate());
        }

        scheduleMentionRepository.deleteBySchedule_ScheduleId(scheduleId);
        if (updateScheduleDto.getMentionUserIds() != null && !updateScheduleDto.getMentionUserIds().isEmpty()) {
            for (Long mentionUserId : updateScheduleDto.getMentionUserIds()) {
                UserEntity mentionUser = userRepository.findById(mentionUserId)
                        .orElseThrow(() -> new AppException(ErrorCode.INVALID_MENTION_USER, ErrorCode.INVALID_MENTION_USER.getMessage()));
                ScheduleMentionEntity scheduleMentionEntity = ScheduleMentionEntity.builder()
                        .schedule(scheduleEntity)
                        .user(mentionUser)
                        .build();
                scheduleMentionRepository.save(scheduleMentionEntity);
            }
        }

        try {
            scheduleRepository.flush();
            eventPublisher.publishEvent(new UpdateScheduleEvent(scheduleId, "일정이 성공적으로 수정되었습니다.", true));

            ScheduleEntity schedule = scheduleRepository.findById(scheduleId)
                    .orElseThrow(() -> new AppException(ErrorCode.SCHEDULE_NOT_FOUND, ErrorCode.SCHEDULE_NOT_FOUND.getMessage()));

            for (UserEntity member : schedule.getCalendar().getUserCalendars().stream().map(UserCalendarEntity::getUserEntity).toList()) {
                alarmService.sendAlarm(member, schedule.getCalendar(), schedule, "event_updated");
            }

            return Collections.singletonList(convertScheduleEntityToUpdateScheduleDto(scheduleEntity));
        } catch (OptimisticLockingFailureException exception) {
            eventPublisher.publishEvent(new UpdateScheduleEvent(scheduleId, "동시 수정 충돌로 인해 수정 실패하였습니다.", false));
            throw new AppException(ErrorCode.NOT_UPDATE, ErrorCode.NOT_UPDATE.getMessage());
        }
    }

    private UpdateScheduleDto convertScheduleEntityToUpdateScheduleDto(ScheduleEntity savedEntity) {
        RepeatScheduleDto repeatScheduleDto = RepeatScheduleDto.builder()
                .repeatType(savedEntity.getRepeatType().name())
                .repeatInterval(savedEntity.getRepeatInterval())
                .repeatEndDate(savedEntity.getRepeatEndDate())
                .build();

        return UpdateScheduleDto.builder()
                .title(savedEntity.getTitle())
                .location(savedEntity.getLocation())
                .startTime(savedEntity.getStartTime())
                .endTime(savedEntity.getEndTime())
                .repeatSchedule(repeatScheduleDto)
                .memo(savedEntity.getMemo())
                .build();
    }

    //일정 삭제
    @Transactional
    public DeleteScheduleDto deleteSchedule(CustomUserDetails customUserDetails, Long scheduleId, Long calendarId) {
        if (customUserDetails == null) {
            throw new AppException(ErrorCode.NOT_FOUND_USER, ErrorCode.NOT_FOUND_USER.getMessage());
        }

        ScheduleEntity scheduleEntity = scheduleRepository.findById(scheduleId)
                .orElseThrow(() -> new AppException(ErrorCode.SCHEDULE_NOT_FOUND, ErrorCode.SCHEDULE_NOT_FOUND.getMessage()));

        CalendarEntity calendarEntity = scheduleEntity.getCalendar();
        if (calendarEntity == null || !calendarEntity.getCalendarId().equals(calendarId)) {
            throw new AppException(ErrorCode.INVALID_CALENDAR_ID,ErrorCode.INVALID_CALENDAR_ID.getMessage());
        }

        Long currentUserId = customUserDetails.getUserEntity().getUserId();
        boolean canDelete;
        if (calendarEntity.getCalendarType().equals(CalendarType.PERSONAL)) {
            canDelete = scheduleEntity.getCreateUserId().getUserId().equals(currentUserId);
        } else {
            // SHARED
            canDelete = userCalendarRepository.existsByCalendarEntityCalendarIdAndUserEntityUserId(calendarEntity.getCalendarId(), currentUserId);
        }
        if (!canDelete) {
            throw new AppException(ErrorCode.UNAUTHORIZED_ACCESS, ErrorCode.UNAUTHORIZED_ACCESS.getMessage());
        }

        try {
            scheduleMentionRepository.deleteBySchedule_ScheduleId(scheduleId);
            scheduleEntity.setScheduleStatus(ScheduleStatus.CANCELLED);
            scheduleRepository.save(scheduleEntity);
            scheduleRepository.flush();

            eventPublisher.publishEvent(new DeleteScheduleEvent(scheduleId, "일정이 성공적으로 삭제되었습니다.", true));

            ScheduleEntity schedule = scheduleRepository.findById(scheduleId)
                    .orElseThrow(() -> new AppException(ErrorCode.SCHEDULE_NOT_FOUND, ErrorCode.SCHEDULE_NOT_FOUND.getMessage()));

            for (UserEntity member : schedule.getCalendar().getUserCalendars().stream().map(UserCalendarEntity::getUserEntity).toList()) {
                alarmService.sendAlarm(member, schedule.getCalendar(), schedule, "event_deleted");
            }

            return DeleteScheduleDto.builder()
                    .scheduleId(scheduleId)
                    .message("일정이 성공적으로 삭제되었습니다.")
                    .build();
        } catch (OptimisticLockingFailureException ex) {
            eventPublisher.publishEvent(new DeleteScheduleEvent(scheduleId, "동시 삭제 충돌로 인해 삭제 실패하였습니다.", false));
            throw new AppException(ErrorCode.NOT_DELETE, ErrorCode.NOT_DELETE.getMessage());
        }
    }

}
