package com.github.scheduler.schedule.service;

import com.github.scheduler.auth.entity.UserEntity;
import com.github.scheduler.auth.repository.UserRepository;
import com.github.scheduler.calendar.entity.CalendarEntity;
import com.github.scheduler.calendar.entity.UserCalendarEntity;
import com.github.scheduler.calendar.repository.UserCalendarRepository;
import com.github.scheduler.global.config.auth.custom.CustomUserDetails;
import com.github.scheduler.global.exception.AppException;
import com.github.scheduler.global.exception.ErrorCode;
import com.github.scheduler.schedule.dto.*;
import com.github.scheduler.schedule.entity.RepeatType;
import com.github.scheduler.schedule.entity.ScheduleStatus;
import com.github.scheduler.schedule.entity.SchedulerEntity;
import com.github.scheduler.schedule.repository.CalendarRepository;
import com.github.scheduler.schedule.repository.ScheduleRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.*;
import java.time.format.DateTimeFormatter;
import java.time.temporal.TemporalAdjusters;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.TimeUnit;
import java.util.function.LongFunction;
import java.util.stream.Collectors;


@Slf4j
@Service
@RequiredArgsConstructor
public class ScheduleService {

    private final UserRepository userRepository;
    private final CalendarRepository calendarRepository;
    private final UserCalendarRepository userCalendarRepository;
    private final ScheduleRepository scheduleRepository;
    private final RedissonClient redissonClient;

    //일정 조회(monthly,weekly,daily)
    @Transactional
    public List<ScheduleDto> getSchedules(CustomUserDetails customUserDetails, String view, String date, String scheduleType) {

        if (customUserDetails == null) {
            throw new AppException(ErrorCode.NOT_FOUND_USER,ErrorCode.NOT_FOUND_USER.getMessage());
        }

        //날짜 계산 필요
        LocalDate startDate;
        LocalDate endDate;

        try {
            if ("monthly".equalsIgnoreCase(view)) {
                DateTimeFormatter monthFormatter = DateTimeFormatter.ofPattern("yyyy-MM");
                YearMonth yearMonth = YearMonth.parse(date, monthFormatter);
                startDate = yearMonth.atDay(1);
                endDate = yearMonth.atEndOfMonth();
            } else if ("weekly".equalsIgnoreCase(view)) {
                LocalDate inputDate = LocalDate.parse(date, DateTimeFormatter.ISO_LOCAL_DATE);
                startDate = inputDate.with(DayOfWeek.MONDAY);
                endDate = inputDate.with(TemporalAdjusters.nextOrSame(DayOfWeek.SUNDAY));
            } else if ("daily".equalsIgnoreCase(view)) {
                startDate = LocalDate.parse(date, DateTimeFormatter.ISO_LOCAL_DATE);
                endDate = startDate;
            } else {
                throw new IllegalArgumentException("지원하지 않는 날짜 형식입니다.");
            }
        } catch (AppException e) {
            throw new AppException(ErrorCode.DATE_FORMAT_INCORRECT, ErrorCode.DATE_FORMAT_INCORRECT.getMessage());
        }

        LocalDateTime startDateTime = startDate.atStartOfDay();
        LocalDateTime endDateTime = endDate.atTime(LocalTime.MAX);

        //사용자가 속한 모든 캘린더 조회
        UserEntity userEntity = customUserDetails.getUserEntity();
        List<CalendarEntity> userCalendars = calendarRepository.findByUserId(userEntity);

        //개인과 공유 캘린더 분류
        List<Long> personalCalendarIds = new ArrayList<>();
        List<Long> sharedCalendarIds = new ArrayList<>();

        for (CalendarEntity calendarEntity : userCalendars) {
            int memberCount = calendarEntity.getUserCalendarEntities() != null
                    ? calendarEntity.getUserCalendarEntities().size()
                    : 0;
            if (memberCount > 1) {
                sharedCalendarIds.add(calendarEntity.getCalendarId());
            }else {
                personalCalendarIds.add(calendarEntity.getCalendarId());
            }
        }

        // 프론트에서 요청한 scheduleType에 따라 대상 캘린더 ID 구성
        List<Long> calendarIds;
        if ("personal".equalsIgnoreCase(scheduleType)) {
            calendarIds = personalCalendarIds;
        } else if ("share".equalsIgnoreCase(scheduleType)) {
            calendarIds = sharedCalendarIds;
        } else if ("both".equalsIgnoreCase(scheduleType)) {
            calendarIds = new ArrayList<>();
            calendarIds.addAll(personalCalendarIds);
            calendarIds.addAll(sharedCalendarIds);
        } else {
            throw new IllegalArgumentException("일정을 조회할 수 없습니다.");
        }
        if (calendarIds.isEmpty()) {
            return Collections.emptyList();
        }

        // 대상 캘린더에 속한 일정 조회
        List<SchedulerEntity> schedulerEntities = scheduleRepository.findByCalendarIdInAndStartTimeBetween(
                calendarIds, startDateTime, endDateTime);

        return schedulerEntities.stream().map(schedulerEntity -> {
                ScheduleDto scheduleDto = new ScheduleDto();
                scheduleDto.setScheduleId(schedulerEntity.getScheduleId());
                scheduleDto.setCreateUserId(schedulerEntity.getCreateUserId().getUserId());
                scheduleDto.setTitle(schedulerEntity.getTitle());
                scheduleDto.setStartTime(schedulerEntity.getStartTime());
                scheduleDto.setEndTime(schedulerEntity.getEndTime());
                scheduleDto.setRepeatSchedule(createRepeatScheduleDto(schedulerEntity));
                scheduleDto.setLocation(schedulerEntity.getLocation());
                scheduleDto.setTodoList(schedulerEntity.getMemo());
                scheduleDto.setCalendarId(schedulerEntity.getCalendarId() != null ? schedulerEntity.getCalendarId().getCalendarId().toString() : null);
                scheduleDto.setStatus(schedulerEntity.getScheduleStatus().name());

                if (schedulerEntity.getCalendarId() != null) {
                    List<Long> calendarMembers = userCalendarReposi
                }

                return scheduleDto;

            }).collect(Collectors.toList());
    }
    private RepeatScheduleDto createRepeatScheduleDto(SchedulerEntity entity) {
        RepeatScheduleDto repeatDto = new RepeatScheduleDto();
        repeatDto.setRepeatType(entity.getRepeatType().name());
        repeatDto.setRepeatInterval(entity.getRepeatInterval());
        repeatDto.setRepeatEndDate(entity.getRepeatEndDate());
        return repeatDto;
    }

    //일정 등록
    @Transactional
    public List<CreateScheduleDto> createSchedule(CustomUserDetails customUserDetails, CreateScheduleDto createScheduleDto) {

        if (customUserDetails == null) {
            throw new AppException(ErrorCode.NOT_FOUND_USER,ErrorCode.NOT_FOUND_USER.getMessage());
        }

        UserEntity userEntity = customUserDetails.getUserEntity();

        RepeatType repeatType;
        try {
            repeatType = RepeatType.valueOf(
                    Optional.ofNullable(createScheduleDto.getRepeatSchedule())
                            .map(RepeatScheduleDto::getRepeatType)
                            .orElse("NONE")
                            .toUpperCase()
            );
        } catch (Exception e) {
            repeatType = RepeatType.NONE;
        }

        String calenderInput = createScheduleDto.getCalendarId();
        boolean isBoth = "BOTH".equalsIgnoreCase(calenderInput);

        List<SchedulerEntity> createdEntities = new ArrayList<>();

        // 개인 일정 등록: 캘린더 ID가 null, 빈 문자열 또는 "BOTH"인 경우
        if (calenderInput == null || calenderInput.trim().isEmpty() || isBoth) {
            SchedulerEntity personalEntity = buildSchedulerEntity(userEntity, createScheduleDto, repeatType, null);
            createdEntities.add(scheduleRepository.save(personalEntity));
        }

        // 팀 일정 등록: 캘린더 ID가 제공되고, 빈 문자열이 아니면서 "BOTH"가 아닌 경우
        if (calenderInput != null && !calenderInput.trim().isEmpty() && !isBoth) {
            CalendarEntity teamCalender = new CalendarEntity();
            try {
                teamCalender.setCalendarId(Long.parseLong(calenderInput));
            } catch (NumberFormatException e) {
                throw new AppException(ErrorCode.INVALID_INPUT,ErrorCode.INVALID_INPUT.getMessage());
            }
            SchedulerEntity teamEntity = buildSchedulerEntity(userEntity, createScheduleDto, repeatType, teamCalender);
            createdEntities.add(scheduleRepository.save(teamEntity));
        }

        return createdEntities.stream()
                .map(this::mapToCreateScheduleDto)
                .collect(Collectors.toList());
    }
    // 공통적으로 SchedulerEntity 를 생성
    private SchedulerEntity buildSchedulerEntity(UserEntity userEntity, CreateScheduleDto dto, RepeatType repeatType, CalendarEntity calEntity) {
        return SchedulerEntity.builder()
                .createUserId(userEntity)
                .title(dto.getTitle())
                .startTime(dto.getStartTime())
                .endTime(dto.getEndTime())
                .repeatType(repeatType)
                .repeatInterval(dto.getRepeatInterval() != null ? dto.getRepeatInterval() : 0)
                .repeatEndDate(dto.getRepeatEndDate())
                .location(dto.getLocation())
                .memo(dto.getTodoList())
                .calendarId(calEntity)
                .scheduleStatus(ScheduleStatus.SCHEDULED)
                .build();
    }
    private CreateScheduleDto mapToCreateScheduleDto(SchedulerEntity entity) {
        CreateScheduleDto dto = new CreateScheduleDto();
        dto.setCreateUserId(entity.getCreateUserId().getUserId());
        dto.setTitle(entity.getTitle());
        dto.setStartTime(entity.getStartTime());
        dto.setEndTime(entity.getEndTime());

        // 반복 설정 DTO 매핑
        RepeatScheduleDto repeatDto = new RepeatScheduleDto();
        repeatDto.setRepeatType(entity.getRepeatType().name());
        repeatDto.setRepeatInterval(entity.getRepeatInterval());
        repeatDto.setRepeatEndDate(entity.getRepeatEndDate());
        dto.setRepeatSchedule(repeatDto);

        dto.setRepeatInterval(entity.getRepeatInterval());
        dto.setRepeatEndDate(entity.getRepeatEndDate());
        dto.setLocation(entity.getLocation());
        dto.setTodoList(entity.getMemo());
        dto.setCalendarId(entity.getCalendarId() != null ? entity.getCalendarId().getCalendarId().toString() : null);
        // 만약 필요한 경우, 추가 필드도 설정 (예: scheduleId 등)
        return dto;
    }

    //TODO:일정 수정
    @Transactional
    public List<UpdateScheduleDto> updateSchedule(CustomUserDetails customUserDetails, UpdateScheduleDto updateScheduleDto,
                                                  Long scheduleId, String scheduleType) {
        if (customUserDetails == null) {
            throw new AppException(ErrorCode.NOT_FOUND_USER, ErrorCode.NOT_FOUND_USER.getMessage());
        }

        // 동시성 제어: Redisson 락 획득
        RLock lock = redissonClient.getLock("schedule:" + scheduleId);
        try {
            boolean acquired = lock.tryLock(5, 5, TimeUnit.SECONDS); // 다른 프로세스가 락을 보유중일 때 최대 5초 동안 대기, 획득 후 5초동안 락 유지, 5초 후 자동 해제
            if (!acquired) {
                throw new AppException(ErrorCode.NOT_OBTAIN_LOCK, ErrorCode.NOT_OBTAIN_LOCK.getMessage());
            }
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }

        List<SchedulerEntity> createdEntities = new ArrayList<>();
        if ("personal".equalsIgnoreCase(scheduleType)){

        }
    }

        //TODO:일정 삭제
    @Transactional
    public DeleteScheduleDto deleteSchedule(CustomUserDetails customUserDetails , DeleteScheduleDto deleteScheduleDto, Long scheduleId){
        if (customUserDetails == null) {
            throw new AppException(ErrorCode.NOT_FOUND_USER,ErrorCode.NOT_FOUND_USER.getMessage());
        }

        // 동시성 제어: Redisson 락 획득
        RLock lock = redissonClient.getLock("schedule:" + scheduleId);
        try {
            boolean acquired = lock.tryLock(5, 5, TimeUnit.SECONDS); // 다른 프로세스가 락을 보유중일 때 최대 5초 동안 대기, 획득 후 5초동안 락 유지, 5초 후 자동 해제
            if (!acquired) {
                throw new AppException(ErrorCode.NOT_OBTAIN_LOCK, ErrorCode.NOT_OBTAIN_LOCK.getMessage());
            }
        } catch (InterruptedException e) {
            throw new RuntimeException(e);

        }

}
