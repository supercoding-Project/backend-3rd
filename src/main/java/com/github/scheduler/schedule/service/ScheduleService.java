package com.github.scheduler.schedule.service;

import com.github.scheduler.auth.entity.UserEntity;
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
import com.github.scheduler.schedule.repository.ScheduleRepository;
import com.github.scheduler.todo.entity.TodoEntity;
import com.github.scheduler.todo.repository.TodoRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.*;
import java.time.temporal.TemporalAdjusters;
import java.util.*;
import java.util.concurrent.TimeUnit;

@Slf4j
@Service
@RequiredArgsConstructor
public class ScheduleService {

    private final CalendarRepository calendarRepository;
    private final UserCalendarRepository userCalendarRepository;
    private final ScheduleRepository scheduleRepository;
    private final RedissonClient redissonClient;

    //일정 조회(monthly,weekly,daily)
    @Transactional
    public List<ScheduleDto> getSchedules(CustomUserDetails customUserDetails, String view, String date, List<CalendarType> calendarType) {

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

        // 사용자 ID 가져오기
        Long userId = customUserDetails.getUserEntity().getUserId();
        List<ScheduleDto> result = new ArrayList<>();

        // 캘린더 타입별 조회
        for (CalendarType type : calendarType) {
            if (type == CalendarType.PERSONAL){
                List<ScheduleEntity> personalSchedules = scheduleRepository.findByCreateUserIdUserIdAndStartTimeBetween(userId, startDateTime, endDateTime);
                for (ScheduleEntity schedule : personalSchedules) {
                    result.add(convertScheduleEntityToDto(schedule));
                }
            } else if (type == CalendarType.SHARED) {
                List<ScheduleEntity> sharedSchedules = scheduleRepository.findByCalendarIdIsNotNullAndStartTimeBetweenAndCreateUserId_UserId(startDateTime, endDateTime, userId);
                for (ScheduleEntity schedule : sharedSchedules) {
                    ScheduleDto dto = convertScheduleEntityToDto(schedule);
                    if (schedule.getCalendarId() != null) {
                        List<UserCalendarEntity> sharedUsers = userCalendarRepository.findByCalendarCalendarId(
                                schedule.getCalendarId().getCalendarId());
                        dto.setSharedUsers(sharedUsers);
                    }
                    result.add(dto);
                }
            }
        }
        // 여러 타입이 선택된 경우, 시작 시간 기준 정렬
        result.sort(Comparator.comparing(ScheduleDto::getStartTime));
        return result;
    }

    // ScheduleEntity -> ScheduleDto 변환 메서드
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
                .calendarId(entity.getCalendarId() != null ? entity.getCalendarId().getCalendarId().toString() : null)
                .status(entity.getScheduleStatus().getScheduleStatus())
                .build();
    }


    //일정 등록
    @Transactional
    public List<CreateScheduleDto> createSchedule(CustomUserDetails customUserDetails, CreateScheduleDto createScheduleDto,
                                                  CalendarType calendarType, Long calendarId) {

        if (customUserDetails == null) {
            throw new AppException(ErrorCode.NOT_FOUND_USER,ErrorCode.NOT_FOUND_USER.getMessage());
        }

        UserEntity userEntity = customUserDetails.getUserEntity();

        // 반복 설정 처리
        RepeatType repeatType = RepeatType.NONE;
        int repeatInterval = 0;
        LocalDate repeatEndDate = null;
        if (createScheduleDto.getRepeatSchedule() != null
                && createScheduleDto.getRepeatSchedule().getRepeatType() != null
                && !createScheduleDto.getRepeatSchedule().getRepeatType().isEmpty()) {
            repeatType = RepeatType.valueOf(createScheduleDto.getRepeatSchedule().getRepeatType().toUpperCase());
            repeatInterval = createScheduleDto.getRepeatSchedule().getRepeatInterval();
            repeatEndDate = createScheduleDto.getRepeatSchedule().getRepeatEndDate();
        }

        ScheduleEntity scheduleEntity;

        if(calendarType == CalendarType.PERSONAL){

            // 개인 일정 등록: 캘린더 ID가 제공되지 않으면 사용자의 기본 개인 캘린더 조회
            CalendarEntity personalCalendar;
            if (calendarId == null) {
                personalCalendar = calendarRepository.findPersonalCalendarByOwnerUserId(userEntity.getUserId())
                        .orElseThrow(() -> new AppException(ErrorCode.PERSONAL_CALENDAR_NOT_FOUND,ErrorCode.PERSONAL_CALENDAR_NOT_FOUND.getMessage()));
            } else {
                personalCalendar = calendarRepository.findById(calendarId)
                        .orElseThrow(() -> new AppException(ErrorCode.PERSONAL_CALENDAR_NOT_FOUND,ErrorCode.PERSONAL_CALENDAR_NOT_FOUND.getMessage()));
            }

            scheduleEntity = ScheduleEntity.builder()
                    .createUserId(userEntity)
                    .title(createScheduleDto.getTitle())
                    .location(createScheduleDto.getLocation())
                    .startTime(createScheduleDto.getStartTime())
                    .endTime(createScheduleDto.getEndTime())
                    .repeatType(repeatType)
                    .repeatInterval(repeatInterval)
                    .repeatEndDate(repeatEndDate)
                    .memo(createScheduleDto.getMemo())
                    .scheduleStatus(ScheduleStatus.SCHEDULED)
                    .calendarId(personalCalendar)
                    .build();

        }else if(calendarType == CalendarType.SHARED){

            CalendarEntity calendarEntity = calendarRepository.findById(createScheduleDto.getCalendarId())
                    .orElseThrow(() -> new AppException(ErrorCode.SHARED_CALENDAR_NOT_FOUND,ErrorCode.SHARED_CALENDAR_NOT_FOUND.getMessage()));

            scheduleEntity = ScheduleEntity.builder()
                    .createUserId(userEntity)
                    .title(createScheduleDto.getTitle())
                    .location(createScheduleDto.getLocation())
                    .startTime(createScheduleDto.getStartTime())
                    .endTime(createScheduleDto.getEndTime())
                    .repeatType(repeatType)
                    .repeatInterval(repeatInterval)
                    .repeatEndDate(repeatEndDate)
                    .memo(createScheduleDto.getMemo())
                    .scheduleStatus(ScheduleStatus.SCHEDULED)
                    .calendarId(calendarEntity)  // 공유 혹은 할일 캘린더 연관 설정
                    .build();
        }else {
            throw new AppException(ErrorCode.INVALID_CALENDAR_TYPE,ErrorCode.INVALID_CALENDAR_TYPE.getMessage());
        }
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
    public List<CreateScheduleDto> updateSchedule(CustomUserDetails customUserDetails, CreateScheduleDto updateScheduleDto,
                                                  Long scheduleId, CalendarType calendarType) {
        if (customUserDetails == null) {
            throw new AppException(ErrorCode.NOT_FOUND_USER, ErrorCode.NOT_FOUND_USER.getMessage());
        }

        RLock lock = redissonClient.getLock("schedule:" + scheduleId);
        boolean acquired = false;
        try {
            acquired = lock.tryLock(5, 5, TimeUnit.SECONDS);
            if (!acquired) {
                throw new AppException(ErrorCode.NOT_OBTAIN_LOCK, ErrorCode.NOT_OBTAIN_LOCK.getMessage());
            }
            ScheduleEntity scheduleEntity = scheduleRepository.findById(scheduleId)
                    .orElseThrow(() -> new AppException(ErrorCode.SCHEDULE_NOT_FOUND, ErrorCode.SCHEDULE_NOT_FOUND.getMessage()));

            Long currentUserId = customUserDetails.getUserEntity().getUserId();
            boolean canUpdate = false;

            //캘린더 타입별 조회
            CalendarEntity calendarEntity = scheduleEntity.getCalendarId(); // CalendarEntity와 연관됨
            if (calendarEntity != null) {
                // 전달받은 캘린더 타입과 실제 캘린더의 타입이 일치하는지 확인
                if (!calendarEntity.getCalendarType().equals(calendarType)) {
                    throw new AppException(ErrorCode.INVALID_CALENDAR_TYPE, "요청한 캘린더 타입과 일정의 캘린더 타입이 일치하지 않습니다.");
                }
                if (calendarType.equals(CalendarType.PERSONAL)) {
                    // 개인 캘린더: 작성자만 수정 가능
                    canUpdate = scheduleEntity.getCreateUserId().getUserId().equals(currentUserId);
                } else if (calendarType.equals(CalendarType.SHARED)) {
                    // 공유 캘린더: 해당 캘린더의 구성원(팀원)이라면 수정 가능
                    if (userCalendarRepository.existsByCalendarCalendarIdAndUserEntityUserId(
                            calendarEntity.getCalendarId(), currentUserId)) {
                        canUpdate = true;
                    }
                } else {
                    //다른 타입도 유사하게 조회
                    if (userCalendarRepository.existsByCalendarCalendarIdAndUserEntityUserId(
                            calendarEntity.getCalendarId(), currentUserId)) {
                        canUpdate = true;
                    }
                }
            }

            if (!canUpdate) {
                throw new AppException(ErrorCode.UNAUTHORIZED_ACCESS, "수정 권한이 없습니다.");
            }

            // 업데이트할 필드 수정
            scheduleEntity.setTitle(updateScheduleDto.getTitle());
            scheduleEntity.setLocation(updateScheduleDto.getLocation());
            scheduleEntity.setStartTime(updateScheduleDto.getStartTime());
            scheduleEntity.setEndTime(updateScheduleDto.getEndTime());
            scheduleEntity.setMemo(updateScheduleDto.getMemo());

            // 반복 설정 업데이트 (옵션)
            if (updateScheduleDto.getRepeatSchedule() != null &&
                    updateScheduleDto.getRepeatSchedule().getRepeatType() != null &&
                    !updateScheduleDto.getRepeatSchedule().getRepeatType().isEmpty()) {
                RepeatType newRepeatType = RepeatType.valueOf(updateScheduleDto.getRepeatSchedule().getRepeatType().toUpperCase());
                scheduleEntity.setRepeatType(newRepeatType);
                scheduleEntity.setRepeatInterval(updateScheduleDto.getRepeatSchedule().getRepeatInterval());
                scheduleEntity.setRepeatEndDate(updateScheduleDto.getRepeatSchedule().getRepeatEndDate());
            }

            ScheduleEntity savedScheduleEntity = scheduleRepository.save(scheduleEntity);
            return Collections.singletonList(convertScheduleEntityToCreateScheduleDto(savedScheduleEntity));
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new AppException(ErrorCode.NOT_OBTAIN_LOCK, ErrorCode.NOT_OBTAIN_LOCK.getMessage());
        } finally {
            if (acquired) {
                lock.unlock();
            }
        }
    }

    //일정 삭제
    @Transactional
    public DeleteScheduleDto deleteSchedule(CustomUserDetails customUserDetails, DeleteScheduleDto deleteScheduleDto,
                                            Long scheduleId, CalendarType calendarType) {
        if (customUserDetails == null) {
            throw new AppException(ErrorCode.NOT_FOUND_USER, ErrorCode.NOT_FOUND_USER.getMessage());
        }

        RLock lock = redissonClient.getLock("schedule:" + scheduleId);
        boolean acquired = false;
        try {
            acquired = lock.tryLock(5, 5, TimeUnit.SECONDS);
            if (!acquired) {
                throw new AppException(ErrorCode.NOT_OBTAIN_LOCK, ErrorCode.NOT_OBTAIN_LOCK.getMessage());
            }
            ScheduleEntity scheduleEntity = scheduleRepository.findById(scheduleId)
                    .orElseThrow(() -> new AppException(ErrorCode.SCHEDULE_NOT_FOUND, ErrorCode.SCHEDULE_NOT_FOUND.getMessage()));
            Long currentUserId = customUserDetails.getUserEntity().getUserId();
            boolean canDelete = false;

            CalendarEntity calendarEntity = scheduleEntity.getCalendarId();
            if (calendarEntity != null) {
                // 전달받은 calendarType 과 실제 일정의 캘린더 타입이 일치하는지 확인
                if (!calendarEntity.getCalendarType().equals(calendarType)) {
                    throw new AppException(ErrorCode.INVALID_CALENDAR_TYPE, "요청한 캘린더 타입과 일정의 캘린더 타입이 일치하지 않습니다.");
                }
                if (calendarType.equals(CalendarType.PERSONAL)) {
                    // 개인 캘린더: 작성자만 삭제 가능
                    canDelete = scheduleEntity.getCreateUserId().getUserId().equals(currentUserId);
                } else if (calendarType.equals(CalendarType.SHARED)) {
                    // 공유 캘린더: 해당 캘린더의 구성원은 삭제 가능
                    if (userCalendarRepository.existsByCalendarCalendarIdAndUserEntityUserId(
                            calendarEntity.getCalendarId(), currentUserId)) {
                        canDelete = true;
                    }
                } else {
                    // 다른 캘린더 타입(예: TODO)도 공유와 유사하게 처리
                    if (userCalendarRepository.existsByCalendarCalendarIdAndUserEntityUserId(
                            calendarEntity.getCalendarId(), currentUserId)) {
                        canDelete = true;
                    }
                }
            } else {
                // 캘린더 연관 정보가 없는 경우: 작성자만 삭제 가능
                canDelete = scheduleEntity.getCreateUserId().getUserId().equals(currentUserId);
            }

            if (!canDelete) {
                throw new AppException(ErrorCode.UNAUTHORIZED_ACCESS, "삭제 권한이 없습니다.");
            }

            scheduleRepository.delete(scheduleEntity);
            return DeleteScheduleDto.builder()
                    .scheduleId(scheduleId)
                    .message("일정이 성공적으로 삭제되었습니다.")
                    .build();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new AppException(ErrorCode.NOT_OBTAIN_LOCK, ErrorCode.NOT_OBTAIN_LOCK.getMessage());
        } finally {
            if (acquired) {
                lock.unlock();
            }
        }
    }
}
