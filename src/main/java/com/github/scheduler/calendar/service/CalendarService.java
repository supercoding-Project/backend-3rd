package com.github.scheduler.calendar.service;

import com.github.scheduler.alarm.service.AlarmService;
import com.github.scheduler.auth.entity.UserEntity;
import com.github.scheduler.auth.repository.UserRepository;
import com.github.scheduler.calendar.dto.CalendarMemberDeleteRequestDto;
import com.github.scheduler.calendar.dto.CalendarMemberResponseDto;
import com.github.scheduler.calendar.dto.CalendarRequestDto;
import com.github.scheduler.calendar.dto.CalendarResponseDto;
import com.github.scheduler.calendar.entity.CalendarEntity;
import com.github.scheduler.calendar.entity.CalendarRole;
import com.github.scheduler.calendar.entity.CalendarType;
import com.github.scheduler.calendar.entity.UserCalendarEntity;
import com.github.scheduler.calendar.event.CalendarJoinedEvent;
import com.github.scheduler.calendar.repository.CalendarRepository;
import com.github.scheduler.calendar.repository.UserCalendarRepository;
import com.github.scheduler.global.dto.ApiResponse;
import com.github.scheduler.global.exception.AppException;
import com.github.scheduler.global.exception.ErrorCode;
import com.github.scheduler.invite.service.EmailService;
import com.github.scheduler.invite.service.InviteCodeService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

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
    private final ApplicationEventPublisher eventPublisher;
    private final AlarmService alarmService;

    // 캘린더 생성
    @Transactional
    public CalendarResponseDto createCalendar(CalendarRequestDto calendarRequestDto, String email) {
        UserEntity userEntity = userRepository.findByEmail(email).orElseThrow(
                () -> new AppException(ErrorCode.NOT_FOUND_USER, ErrorCode.NOT_FOUND_USER.getMessage())
        );

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

        CalendarEntity calendarEntity = CalendarEntity.builder()
                .calendarName(calendarRequestDto.getCalendarName())
                .owner(userEntity)
                .calendarDescription(calendarRequestDto.getCalendarDescription())
                .calendarType(calendarType)
                .createdAt(now)
                .build();
        calendarRepository.save(calendarEntity);

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
                .calendarDescription(calendarEntity.getCalendarDescription())
                .calendarType(calendarEntity.getCalendarType().getType())
                .calendarRole(userCalendarEntity.getRole().getType())
                .createdAt(LocalDateTime.now())
                .build();
    }

    // 캘린더 초대코드 전송
    @Transactional
    public ApiResponse<String> sendInviteCodesByEmail(Long calendarId, String ownerEmail, List<String> emailList) {
        log.info("초대 코드 전송 요청 - 캘린더 ID: {}, 요청자: {}, 대상자 수: {}", calendarId, ownerEmail, emailList.size());

        CalendarEntity calendar = calendarRepository.findById(calendarId)
                .orElseThrow(() -> new AppException(ErrorCode.NOT_FOUND_CALENDAR, "존재하지 않는 캘린더입니다."));

        if (!calendar.getOwner().getEmail().equals(ownerEmail)) {
            throw new AppException(ErrorCode.UNAUTHORIZED_CALENDAR, ErrorCode.UNAUTHORIZED_CALENDAR.getMessage());
        }

        if (calendar.getCalendarType() != CalendarType.SHARED) {
            throw new AppException(ErrorCode.NOT_SHARED_CALENDAR, "공용 캘린더(SHARED)에서만 초대가 가능합니다.");
        }

        log.info("{}는 캘린더 {}의 Owner입니다.", ownerEmail, calendarId);

        String inviteCode = inviteCodeService.getInviteCode(calendarId);
        if (inviteCode == null) {
            inviteCode = inviteCodeService.generateAndSaveInviteCode(calendarId);
            log.info("새로운 초대 코드 생성: {}", inviteCode);
        }

        emailService.sendInviteEmails(emailList, inviteCode, calendarId);
        log.info("초대 코드 {}가 {}명에게 이메일 전송", inviteCode, emailList.size());

        // 초대받은 각 사용자에게 알림 전송
        for (String email : emailList) {
            UserEntity invitedUser = userRepository.findByEmail(email)
                    .orElseThrow(() -> new AppException(ErrorCode.NOT_FOUND_USER, "초대받은 사용자를 찾을 수 없습니다."));

            alarmService.sendInvitationAlarms(calendar, invitedUser);
        }

        return ApiResponse.success("초대 코드가 이메일로 전송되었습니다.");
    }

    // 캘린더 가입
    @Transactional
    public void joinCalendar(String email, String inviteCode) {
        Long calendarId = inviteCodeService.validateInviteCode(inviteCode);
        log.info("Redis에서 조회된 calendarId: {}", calendarId);

        UserEntity userEntity = userRepository.findByEmail(email)
                .orElseThrow(() -> new AppException(ErrorCode.NOT_FOUND_USER, "사용자를 찾을 수 없습니다."));
        log.info("사용자 정보 조회 완료 - email: {}", email);

        CalendarEntity calendarEntity = calendarRepository.findByCalendarIdWithOwner(calendarId);
        log.info("캘린더 조회 완료 - 캘린더 ID: {}, Owner: {}", calendarEntity.getCalendarId(), calendarEntity.getOwner().getEmail());

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

        // 초대 알림 전송 (캘린더의 기존 멤버들에게 알림 전송)
        alarmService.sendInvitationAlarms(calendarEntity, userEntity);

        // 이벤트 발행 (예: 소켓 알림 등 후처리 리스너 연결 가능)
        eventPublisher.publishEvent(new CalendarJoinedEvent(calendarEntity, userEntity));
    }

    // 유저 캘린더 조회
    @Transactional(readOnly = true)
    public List<CalendarResponseDto> getUserCalendars(String email) {
        UserEntity userEntity = userRepository.findByEmail(email)
                .orElseThrow(() -> new AppException(ErrorCode.NOT_FOUND_USER, ErrorCode.NOT_FOUND_USER.getMessage()));

        List<UserCalendarEntity> userCalendars = userCalendarRepository.findByUserEntity(userEntity);

        return userCalendars.stream()
                .map(uc -> new CalendarResponseDto(
                        uc.getCalendarEntity().getCalendarId(),
                        uc.getCalendarEntity().getCalendarName(),
                        uc.getCalendarEntity().getCalendarType().getType(),
                        uc.getRole().getType(),
                        uc.getCalendarEntity().getCalendarDescription(),
                        uc.getCalendarEntity().getCreatedAt()
                ))
                .collect(Collectors.toList());
    }

    // 캘린더 소유권 이전
    @Transactional
    public void transferCalendarOwnerships(UserEntity userEntity) {
        log.info("소유권 이전 시작 - 기존 OWNER: {}, ID: {}", userEntity.getEmail(), userEntity.getUserId());

        List<CalendarEntity> ownedCalendars = calendarRepository.findAllByOwner(userEntity);
        log.info("유저가 소유한 캘린더 개수: {}", ownedCalendars.size());

        for (CalendarEntity calendar : ownedCalendars) {
            List<UserCalendarEntity> members = userCalendarRepository.findByCalendarEntity(calendar).stream()
                    .filter(uc -> !uc.getUserEntity().equals(userEntity))
                    .sorted(Comparator.comparing(UserCalendarEntity::getJoinedAt))
                    .toList();

            if (members.isEmpty()) {
                log.info("캘린더 삭제 - 다른 참여자 없음");
                userCalendarRepository.deleteByCalendarEntity(calendar);
                calendarRepository.delete(calendar);
                continue;
            }

            UserCalendarEntity newOwnerUC = members.get(0);
            UserEntity newOwner = userRepository.findById(newOwnerUC.getUserEntity().getUserId())
                    .orElseThrow(() -> new AppException(ErrorCode.NOT_FOUND_USER, "새로운 소유자를 찾을 수 없습니다."));

            log.info("새로운 OWNER 설정 - 기존: {}, 새로운: {}", userEntity.getEmail(), newOwner.getEmail());
            calendar.setOwner(newOwner);
            calendarRepository.save(calendar);

            newOwnerUC.setRole(CalendarRole.OWNER);
            userCalendarRepository.save(newOwnerUC);

            log.info("기존 OWNER의 UserCalendar 삭제: {}", userEntity.getEmail());
            userCalendarRepository.deleteByUserEntityAndCalendarEntity(userEntity, calendar);
        }
    }

    @Transactional
    public void removeMembersFromCalendar(Long calendarId, String ownerEmail, List<String> targetEmails) {
        CalendarEntity calendar = calendarRepository.findById(calendarId)
                .orElseThrow(() -> new AppException(ErrorCode.NOT_FOUND_CALENDAR, "존재하지 않는 캘린더입니다."));

        if (!calendar.getOwner().getEmail().equals(ownerEmail)) {
            throw new AppException(ErrorCode.UNAUTHORIZED_CALENDAR, "해당 캘린더에 대한 권한이 없습니다.");
        }

        for (String targetEmail : targetEmails) {
            if (ownerEmail.equals(targetEmail)) {
                log.warn("자기 자신은 삭제할 수 없습니다: {}", targetEmail);
                continue;
            }

            Optional<UserEntity> targetUserOpt = userRepository.findByEmail(targetEmail);
            if (targetUserOpt.isEmpty()) {
                log.warn("존재하지 않는 유저: {}", targetEmail);
                continue;
            }
            UserEntity targetUser = targetUserOpt.get();

            Optional<UserCalendarEntity> userCalendarOpt = userCalendarRepository.findByUserEntityAndCalendarEntity(targetUser, calendar);
            if (userCalendarOpt.isEmpty()) {
                log.warn("캘린더에 참여하지 않은 유저: {}", targetEmail);
                continue;
            }

            UserCalendarEntity userCalendar = userCalendarOpt.get();

            if (userCalendar.getRole() == CalendarRole.OWNER) {
                log.warn("OWNER는 삭제할 수 없습니다: {}", targetEmail);
                continue;
            }

            userCalendarRepository.delete(userCalendar);
            log.info("{}가 캘린더 [{}] 에서 제거됨", targetEmail, calendar.getCalendarName());
        }
    }

    // 공용 캘린더에 속한 멤버 조회
    @Transactional(readOnly = true)
    public List<CalendarMemberResponseDto> getCalendarMembers(Long calendarId, String requesterEmail) {
        CalendarEntity calendar = calendarRepository.findById(calendarId)
                .orElseThrow(() -> new AppException(ErrorCode.NOT_FOUND_CALENDAR, ErrorCode.NOT_FOUND_CALENDAR.getMessage()));

        UserEntity requester = userRepository.findByEmail(requesterEmail)
                .orElseThrow(() -> new AppException(ErrorCode.USER_EMAIL_NOT_FOUND, ErrorCode.USER_EMAIL_NOT_FOUND.getMessage()));

        boolean isParticipant = userCalendarRepository.existsByUserEntityAndCalendarEntity(requester, calendar);
        if (!isParticipant) {
            throw new AppException(ErrorCode.UNAUTHORIZED_CALENDAR, ErrorCode.UNAUTHORIZED_CALENDAR.getMessage());
        }

        List<UserCalendarEntity> userCalendars = userCalendarRepository.findByCalendarEntity(calendar);

        return userCalendars.stream()
                .map(uc -> new CalendarMemberResponseDto(
                        uc.getUserEntity().getEmail(),
                        uc.getUserEntity().getUsername(),
                        uc.getRole()
                ))
                .collect(Collectors.toList());
    }
}
