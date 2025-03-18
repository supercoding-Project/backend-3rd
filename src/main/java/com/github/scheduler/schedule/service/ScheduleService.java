package com.github.scheduler.schedule.service;

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
import com.github.scheduler.schedule.entity.ScheduleStatus;
import com.github.scheduler.schedule.event.DeleteScheduleEvent;
import com.github.scheduler.schedule.event.UpdateScheduleEvent;
import com.github.scheduler.schedule.repository.ScheduleRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.*;
import java.time.temporal.TemporalAdjusters;
import java.util.*;

@Slf4j
@Service
@RequiredArgsConstructor
public class ScheduleService {

    private final CalendarRepository calendarRepository;
    private final UserCalendarRepository userCalendarRepository;
    private final ScheduleRepository scheduleRepository;
    private final ApplicationEventPublisher eventPublisher;


    //일정 조회(monthly,weekly,daily)
    @Transactional
    public List<ScheduleDto> getSchedules(CustomUserDetails customUserDetails, String view, String date, Long calendarId) {

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

        List<ScheduleEntity> scheduleEntities = scheduleRepository.findByCalendarCalendarIdAndStartTimeBetween(calendarId, startDateTime, endDateTime);
        List<ScheduleDto> result = new ArrayList<>();
        for (ScheduleEntity schedule : scheduleEntities) {
            ScheduleDto dto = convertScheduleEntityToDto(schedule);
            // 캘린더가 존재하고 캘린더 타입이 SHARED 인 경우, 해당 캘린더에 속한 공유 사용자들을 조회
            if (schedule.getCalendar() != null &&
                    schedule.getCalendar().getCalendarType().equals(CalendarType.SHARED)) {
                List<UserCalendarEntity> sharedUsers = userCalendarRepository.findByCalendarEntityCalendarId(
                        schedule.getCalendar().getCalendarId());
                dto.setSharedUsers(sharedUsers);
            }
            result.add(dto);
        }

        // 여러 타입이 선택된 경우, 시작 시간 기준 정렬
        result.sort(Comparator.comparing(ScheduleDto::getStartTime));
        return result;
    }

    // ScheduleEntity -> ScheduleDto
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
        if (!(calendarEntity.getCalendarType().equals(CalendarType.PERSONAL) ||
                calendarEntity.getCalendarType().equals(CalendarType.SHARED))) {
            throw new AppException(ErrorCode.INVALID_CALENDAR_TYPE, ErrorCode.INVALID_CALENDAR_TYPE.getMessage());
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
        CreateScheduleDto saveCreateScheduleDto = convertScheduleEntityToCreateScheduleDto(savedScheduleEntity);
        return Collections.singletonList(saveCreateScheduleDto);
    }

    private CreateScheduleDto convertScheduleEntityToCreateScheduleDto(ScheduleEntity savedEntity) {
        RepeatScheduleDto repeatScheduleDto = RepeatScheduleDto.builder()
                .repeatType(savedEntity.getRepeatType().name())
                .repeatInterval(savedEntity.getRepeatInterval())
                .repeatEndDate(savedEntity.getRepeatEndDate())
                .build();

        return CreateScheduleDto.builder()
                .createUserId(savedEntity.getCreateUserId().getUserId())
                .title(savedEntity.getTitle())
                .location(savedEntity.getLocation())
                .startTime(savedEntity.getStartTime())
                .endTime(savedEntity.getEndTime())
                .repeatSchedule(repeatScheduleDto)
                .memo(savedEntity.getMemo())
                .build();
    }

    //일정 수정
    @Transactional
    public List<UpdateScheduleDto> updateSchedule(CustomUserDetails customUserDetails, UpdateScheduleDto updateScheduleDto,
                                                  Long scheduleId, Long calendarId) {
        if (customUserDetails == null) {
            throw new AppException(ErrorCode.NOT_FOUND_USER, ErrorCode.NOT_FOUND_USER.getMessage());
        }

        ScheduleEntity scheduleEntity = scheduleRepository.findById(scheduleId)
                .orElseThrow(() -> new AppException(ErrorCode.SCHEDULE_NOT_FOUND, ErrorCode.SCHEDULE_NOT_FOUND.getMessage()));

        CalendarEntity calendarEntity = scheduleEntity.getCalendar();
        if (calendarEntity == null || !calendarEntity.getCalendarId().equals(calendarId)) {
            throw new AppException(ErrorCode.INVALID_CALENDAR_TYPE, ErrorCode.INVALID_CALENDAR_TYPE.getMessage());
        }

        Long currentUserId = customUserDetails.getUserEntity().getUserId();
        boolean canUpdate;

        if (calendarEntity.getCalendarType().equals(CalendarType.PERSONAL)) {
            //PERSONAL 경우 작성자만 수정 가능
            canUpdate = scheduleEntity.getCreateUserId().getUserId().equals(currentUserId);
        } else {
            //SHARED
            canUpdate = userCalendarRepository.existsByCalendarEntityCalendarIdAndUserEntityUserId(
                    calendarEntity.getCalendarId(), currentUserId);
        }

        if (!canUpdate) {
            throw new AppException(ErrorCode.UNAUTHORIZED_ACCESS, ErrorCode.UNAUTHORIZED_ACCESS.getMessage());
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

        ScheduleEntity savedScheduleEntity = scheduleRepository.save(scheduleEntity);
        eventPublisher.publishEvent(new UpdateScheduleEvent(savedScheduleEntity.getScheduleId(), "일정이 성공적으로 수정되었습니다."));
        return Collections.singletonList(convertScheduleEntityToUpdateScheduleDto(savedScheduleEntity));
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
    public DeleteScheduleDto deleteSchedule(CustomUserDetails customUserDetails, DeleteScheduleDto deleteScheduleDto,
                                            Long scheduleId, Long calendarId) {
        if (customUserDetails == null) {
            throw new AppException(ErrorCode.NOT_FOUND_USER, ErrorCode.NOT_FOUND_USER.getMessage());
        }

        ScheduleEntity scheduleEntity = scheduleRepository.findById(scheduleId)
                .orElseThrow(() -> new AppException(ErrorCode.SCHEDULE_NOT_FOUND, ErrorCode.SCHEDULE_NOT_FOUND.getMessage()));

        CalendarEntity calendarEntity = scheduleEntity.getCalendar();
        if (calendarEntity == null || !calendarEntity.getCalendarId().equals(calendarId)) {
            throw new AppException(ErrorCode.INVALID_CALENDAR_TYPE,ErrorCode.INVALID_CALENDAR_TYPE.getMessage());
        }

        Long currentUserId = customUserDetails.getUserEntity().getUserId();
        boolean canDelete;
        if (calendarEntity.getCalendarType().equals(CalendarType.PERSONAL)) {
            canDelete = scheduleEntity.getCreateUserId().getUserId().equals(currentUserId);
        } else {
            // SHARED
            canDelete = userCalendarRepository.existsByCalendarEntityCalendarIdAndUserEntityUserId(
                    calendarEntity.getCalendarId(), currentUserId);
        }
        if (!canDelete) {
            throw new AppException(ErrorCode.UNAUTHORIZED_ACCESS, ErrorCode.UNAUTHORIZED_ACCESS.getMessage());
        }

        scheduleRepository.delete(scheduleEntity);

        eventPublisher.publishEvent(new DeleteScheduleEvent(scheduleId, "일정이 성공적으로 삭제되었습니다."));

        String responseMessage = (deleteScheduleDto.getMessage() != null && !deleteScheduleDto.getMessage().isEmpty())
                ? deleteScheduleDto.getMessage()
                : "일정이 성공적으로 삭제되었습니다.";

        return DeleteScheduleDto.builder()
                .scheduleId(scheduleId)
                .message(responseMessage)
                .build();

    }
}
