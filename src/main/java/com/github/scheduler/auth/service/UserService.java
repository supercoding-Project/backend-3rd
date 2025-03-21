package com.github.scheduler.auth.service;

import com.github.scheduler.auth.dto.LoginDto;
import com.github.scheduler.auth.dto.SignUpDto;
import com.github.scheduler.auth.entity.RefreshTokenEntity;
import com.github.scheduler.auth.entity.Role;
import com.github.scheduler.auth.entity.UserEntity;
import com.github.scheduler.auth.repository.RefreshTokenRepository;
import com.github.scheduler.auth.repository.UserRepository;
import com.github.scheduler.calendar.entity.CalendarEntity;
import com.github.scheduler.calendar.repository.CalendarRepository;
import com.github.scheduler.calendar.repository.UserCalendarRepository;
import com.github.scheduler.calendar.service.CalendarService;
import com.github.scheduler.global.config.auth.JwtTokenProvider;
import com.github.scheduler.global.exception.AppException;
import com.github.scheduler.global.exception.ErrorCode;
import com.github.scheduler.global.util.PasswordUtil;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import java.time.LocalDateTime;
import java.util.*;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserService {
    private final UserRepository userRepository;
    private final JwtTokenProvider jwtTokenProvider;
    private final PasswordUtil passwordUtil = new PasswordUtil();
    private final UserImageService userImageService;
    private final RefreshTokenRepository refreshTokenRepository;
    private final CalendarService calendarService;
    private final UserCalendarRepository userCalendarRepository;
    private final CalendarRepository calendarRepository;

    // 회원가입
    @Transactional
    public void signUp(SignUpDto signUpDto, MultipartFile image) throws Exception {
        if (userRepository.findByEmail(signUpDto.getEmail()).isPresent() ||
                userRepository.findByUsername(signUpDto.getUsername()).isPresent()) {
            throw new AppException(ErrorCode.USERNAME_DUPLICATED, "이미 존재하는 이메일 또는 유저네임입니다.");
        }

        UserEntity userEntity = UserEntity.builder()
                .email(signUpDto.getEmail())
                .password(passwordUtil.encrypt(signUpDto.getPassword()))
                .username(signUpDto.getUsername())
                .phone(signUpDto.getPhone())
                .role(Role.ROLE_USER)
                .createdAt(LocalDateTime.now())
                .build();

        boolean isDefaultImage = false;
        if (image == null || image.isEmpty()) {
            image = userImageService.getDefaultProfileImage();
            isDefaultImage = true;
        }

        userImageService.uploadUserImage(userEntity, image, isDefaultImage);

        try {
            userRepository.save(userEntity);
        } catch (DataIntegrityViolationException e) {
            throw new AppException(ErrorCode.USERNAME_DUPLICATED, "이미 존재하는 이메일 또는 유저네임입니다.");
        }

        log.info("✅ 회원가입 완료 - 이메일: {}", userEntity.getEmail());
    }

    // 이메일 체크
    public boolean isEmailAvailable(String email) {
        return !userRepository.existsByEmail(email);
    }

    // 로그인
    @Transactional
    public Map<String, String> login(LoginDto loginDto, HttpServletResponse httpServletResponse) {
        UserEntity userEntity = userRepository.findByEmail(loginDto.getEmail())
                .orElseThrow(() -> new AppException(ErrorCode.CHECK_EMAIL_OR_PASSWORD, ErrorCode.CHECK_EMAIL_OR_PASSWORD.getMessage()));

        if (!Objects.equals(passwordUtil.encrypt(loginDto.getPassword()), userEntity.getPassword())) {
            throw new AppException(ErrorCode.NOT_EQUAL_PASSWORD, ErrorCode.NOT_EQUAL_PASSWORD.getMessage());
        }

        String accessToken = jwtTokenProvider.createAccessToken(userEntity.getEmail());
        String refreshToken = jwtTokenProvider.createRefreshToken(userEntity.getEmail());

        httpServletResponse.addHeader("Authorization", "Bearer " + accessToken);
        Cookie refreshCookie = new Cookie("refresh_token", refreshToken);
        refreshCookie.setHttpOnly(true);
        refreshCookie.setPath("/");
        httpServletResponse.addCookie(refreshCookie);

        addRefresh(userEntity.getEmail(), refreshToken, 10800);

        return Map.of(
                "isAuth", userEntity.getRole() != null ? "true" : "false",
                "message", "로그인 되었습니다",
                "token_type", "Bearer",
                "access_token", accessToken,
                "refresh_token", refreshToken,
                "email", userEntity.getEmail(),
                "username", userEntity.getUsername()
        );
    }

    // 토큰 재발급
    @Transactional
    public Map<String, String> refreshToken(HttpServletRequest request, HttpServletResponse response) {
        String refreshToken = findRefreshTokenCookie(request);

        if (refreshToken == null || !jwtTokenProvider.validateToken(refreshToken)) {
            throw new AppException(ErrorCode.INVALID_REFRESH_TOKEN, ErrorCode.INVALID_REFRESH_TOKEN.getMessage());
        }

        String email = jwtTokenProvider.getEmailByToken(refreshToken);
        UserEntity userEntity = userRepository.findByEmail(email)
                .orElseThrow(() -> new AppException(ErrorCode.USER_EMAIL_NOT_FOUND, ErrorCode.USER_EMAIL_NOT_FOUND.getMessage()));

        String newAccessToken = jwtTokenProvider.createAccessToken(email);
        String newRefreshToken = jwtTokenProvider.createRefreshToken(email);
        Role role = userEntity.getRole();

        jwtTokenProvider.setRole(newAccessToken, role.getType());

        response.addHeader("Authorization", "Bearer " + newAccessToken);
        Cookie refreshCookie = new Cookie("refresh_token", newRefreshToken);
        refreshCookie.setHttpOnly(true);
        refreshCookie.setPath("/");
        response.addCookie(refreshCookie);

        addRefresh(email, newRefreshToken, 720);

        return Map.of(
                "email", email,
                "new_access_token", newAccessToken,
                "new_refresh_token", newRefreshToken,
                "message", "토큰이 갱신되었습니다"
        );
    }

    @PersistenceContext
    private EntityManager entityManager;

    // 회원 탈퇴
    @Transactional
    public void withdrawalUser(String loginEmail, String requestBodyPassword, HttpSession httpSession) {
        UserEntity userEntity = userRepository.findByEmail(loginEmail)
                .orElseThrow(() -> new AppException(ErrorCode.USER_EMAIL_NOT_FOUND, ErrorCode.USER_EMAIL_NOT_FOUND.getMessage()));

        if (!Objects.equals(passwordUtil.encrypt(requestBodyPassword), userEntity.getPassword())) {
            throw new AppException(ErrorCode.NOT_EQUAL_PASSWORD, ErrorCode.NOT_EQUAL_PASSWORD.getMessage());
        }

        List<CalendarEntity> ownedCalendars = calendarRepository.findAllByOwner(userEntity);
        log.info("유저가 소유한 캘린더 개수: {}", ownedCalendars.size());

        if (!ownedCalendars.isEmpty()) {
            log.info("유저가 OWNER인 캘린더 존재 - 소유권 이전 시도");
            calendarService.transferCalendarOwnerships(userEntity);
        }

        userCalendarRepository.deleteByUserEntity(userEntity);
        refreshTokenRepository.deleteByUserEntity(userEntity);
        userRepository.delete(userEntity);

        entityManager.flush();
        entityManager.clear();
        log.info("Hibernate 캐시 정리 완료");

        httpSession.invalidate();
        SecurityContextHolder.clearContext();
        log.info("회원탈퇴 완료 - ID: {}", userEntity.getUserId());
    }

    // 쿠키에서 리프레시 토큰 찾기
    public String findRefreshTokenCookie(HttpServletRequest request) {
        Cookie[] cookies = request.getCookies();
        if (cookies == null) {
            throw new AppException(ErrorCode.NOT_FOUND_COOKIE, ErrorCode.NOT_FOUND_COOKIE.getMessage());
        }
        for (Cookie cookie : cookies) {
            if ("refresh_token".equals(cookie.getName())) {
                if (cookie.getValue() == null || cookie.getValue().isEmpty()) {
                    throw new AppException(ErrorCode.NOT_FOUND_REFRESH_TOKEN, ErrorCode.NOT_FOUND_REFRESH_TOKEN.getMessage());
                }
                return cookie.getValue();
            }
        }
        throw new AppException(ErrorCode.NOT_FOUND_REFRESH_TOKEN, ErrorCode.NOT_FOUND_REFRESH_TOKEN.getMessage());
    }

    // 리프레시 토큰 추가
    protected void addRefresh(String email, String refreshToken, int expiredMinute) {
        LocalDateTime expirationDate = LocalDateTime.now().plusMinutes(expiredMinute);

        UserEntity userEntity = userRepository.findByEmail(email)
                .orElseThrow(() -> new AppException(ErrorCode.USER_EMAIL_NOT_FOUND, ErrorCode.USER_EMAIL_NOT_FOUND.getMessage()));

        RefreshTokenEntity refreshTokenEntity = refreshTokenRepository.findByUserEntity(userEntity)
                .orElseGet(() -> new RefreshTokenEntity(userEntity, refreshToken, expirationDate));

        refreshTokenEntity.setRefreshToken(refreshToken);
        refreshTokenEntity.setExpiration(expirationDate);

        refreshTokenRepository.save(refreshTokenEntity);

        log.info("리프레시 토큰 저장 - 이메일: {}, 만료시간: {}", email, expirationDate);
    }
}
