package com.github.scheduler.calendar.service;

import com.github.scheduler.auth.entity.UserEntity;
import com.github.scheduler.auth.repository.UserRepository;
import com.github.scheduler.calendar.dto.CalendarRequestDto;
import com.github.scheduler.calendar.dto.CalendarResponseDto;
import com.github.scheduler.calendar.entity.CalendarEntity;
import com.github.scheduler.calendar.entity.CalendarRole;
import com.github.scheduler.calendar.entity.CalendarType;
import com.github.scheduler.calendar.entity.UserCalendarEntity;
import com.github.scheduler.calendar.repository.CalendarRepository;
import com.github.scheduler.calendar.repository.UserCalendarRepository;
import com.github.scheduler.global.exception.AppException;
import com.github.scheduler.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDateTime;

@Slf4j
@Service
@RequiredArgsConstructor
public class CalendarService {

    private final UserRepository userRepository;
    private final CalendarRepository calendarRepository;
    private final UserCalendarRepository userCalendarRepository;

    // 캘린더 생성
    @Transactional
    public CalendarResponseDto createCalendar(CalendarRequestDto calendarRequestDto, String email) {
        UserEntity userEntity = userRepository.findByEmail(email).orElseThrow(
                () -> new AppException(ErrorCode.NOT_FOUND_USER, ErrorCode.NOT_FOUND_USER.getMessage())
        );

        // 캘린더 타입 변환
        CalendarType calendarType;
        try {
            calendarType = CalendarType.fromString(calendarRequestDto.getCalendarType().trim());
        } catch (IllegalArgumentException e) {
            throw new AppException(ErrorCode.INVALID_CALENDAR_TYPE,
                    ErrorCode.INVALID_CALENDAR_TYPE.getMessage() +
                            " 입력값: " + calendarRequestDto.getCalendarType() +
                            ". 허용된 값: PERSONAL, SHARED, TODO");
        }

        boolean isDuplicate = calendarRepository.existsByCalendarNameAndCalendarType(calendarRequestDto.getCalendarName(), calendarType);
        if (isDuplicate) {
            throw new AppException(ErrorCode.CALENDAR_NAME_DUPLICATED,
                    String.format("%s(%s)은 이미 존재하는 이름입니다.", calendarRequestDto.getCalendarName(), calendarType));
        }

        LocalDateTime now = LocalDateTime.now();

        // 캘린더 생성
        CalendarEntity calendarEntity = CalendarEntity.builder()
                .calendarName(calendarRequestDto.getCalendarName())
                .owner(userEntity)
                .calendarType(calendarType)
                .createdAt(now)
                .build();
        calendarRepository.save(calendarEntity);

        // UserCalendarEntity에 OWNER로 추가
        UserCalendarEntity userCalendarEntity = UserCalendarEntity.builder()
                .userEntity(userEntity)
                .calendar(calendarEntity)
                .role(CalendarRole.OWNER)
                .joinedAt(now)
                .build();
        userCalendarRepository.save(userCalendarEntity);

        return CalendarResponseDto.builder()
                .calendarId(calendarEntity.getCalendarId())
                .calendarName(calendarEntity.getCalendarName())
                .calendarType(calendarEntity.getCalendarType().getType())
                .calendarRole(userCalendarEntity.getRole().getType())
                .createdAt(calendarEntity.getCreatedAt())
                .build();
    }

    // TODO: 캘린더에 멤버 초대하기

    // TODO: 캘린더에 멤버 삭제하기
}
