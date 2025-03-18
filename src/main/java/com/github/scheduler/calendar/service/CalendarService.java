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
import com.github.scheduler.global.dto.ApiResponse;
import com.github.scheduler.global.exception.AppException;
import com.github.scheduler.global.exception.ErrorCode;
import com.github.scheduler.invite.service.EmailService;
import com.github.scheduler.invite.service.InviteCodeService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class CalendarService {

    private final UserRepository userRepository;
    private final CalendarRepository calendarRepository;
    private final UserCalendarRepository userCalendarRepository;
    private final InviteCodeService inviteCodeService;
    private final EmailService emailService;
    private final StringRedisTemplate redisTemplate;

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
            throw new AppException(ErrorCode.DUPLICATED_CALENDAR_NAME,
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
                .calendarEntity(calendarEntity)
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

    // 공용 캘린더 초대코드 이메일로 전송
    public ApiResponse<String> sendInviteCodesByEmail(Long calendarId, String ownerEmail, List<String> emailList) {
        log.info("📩 초대 코드 전송 요청 - 캘린더 ID: {}, 요청자: {}, 대상자 수: {}", calendarId, ownerEmail, emailList.size());

        // 캘린더 Owner 인지 확인
        CalendarEntity calendar = calendarRepository.findById(calendarId)
                .orElseThrow(() -> new AppException(ErrorCode.NOT_FOUND_CALENDAR, "존재하지 않는 캘린더입니다."));

        if (!calendar.getOwner().getEmail().equals(ownerEmail)) {
            throw new AppException(ErrorCode.UNAUTHORIZED_CALENDAR, ErrorCode.UNAUTHORIZED_CALENDAR.getMessage());
        }

        log.info("요청자 {}는 캘린더 {}의 소유자입니다.", ownerEmail, calendarId);

        String inviteCode = inviteCodeService.getInviteCode(calendarId);

        if (inviteCode == null) {
            inviteCode = inviteCodeService.generateAndSaveInviteCode(calendarId);
            log.info("새로운 초대 코드 생성: {}", inviteCode);
        }

        // 이메일 전송
        emailService.sendInviteEmails(emailList, inviteCode, calendarId);
        log.info("초대 코드 {}가 {}명에게 이메일 전송", inviteCode, emailList.size());

        return ApiResponse.success("초대 코드가 이메일로 전송되었습니다.");
    }

    @Transactional
    public void joinCalendar(String email, String inviteCode) {
        // Redis 에서 초대 코드 검증 및 calendarId 조회
        Long calendarId = inviteCodeService.validateInviteCode(inviteCode);
        log.info("Redis에서 조회된 calendarId: {}", calendarId);

        UserEntity userEntity = userRepository.findByEmail(email)
                .orElseThrow(() -> new AppException(ErrorCode.NOT_FOUND_USER, "사용자를 찾을 수 없습니다."));
        log.info("사용자 정보 조회 완료 - email: {}", email);

        // 캘린더 조회 (Owner 정보 포함)
        CalendarEntity calendarEntity = calendarRepository.findByCalendarIdWithOwner(calendarId);
        log.info("캘린더 조회 완료 - 캘린더 ID: {}, 소유자: {}", calendarEntity.getCalendarId(), calendarEntity.getOwner().getEmail());

        if (calendarEntity.getCalendarType() != CalendarType.SHARED) {
            throw new AppException(ErrorCode.NOT_SHARED_CALENDAR, "공용 캘린더가 아닙니다.");
        }

        if (userCalendarRepository.existsByUserEntityAndCalendarEntity(userEntity, calendarEntity)) {
            throw new AppException(ErrorCode.DUPLICATED_CALENDAR, "이미 가입된 캘린더입니다.");
        }

        UserCalendarEntity userCalendarEntity = UserCalendarEntity.builder()
                .userEntity(userEntity)
                .calendarEntity(calendarEntity)
                .role(CalendarRole.MEMBER)
                .joinedAt(LocalDateTime.now())
                .build();
        userCalendarRepository.save(userCalendarEntity);

        log.info("유저 캘린더 추가 완료 - 사용자: {}, 캘린더 ID: {}", email, calendarEntity.getCalendarId());
    }

    // TODO: 캘린더에 멤버 삭제하기
}
