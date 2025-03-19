package com.github.scheduler.auth.service;

import com.github.scheduler.auth.dto.LoginDto;
import com.github.scheduler.auth.dto.SignUpDto;
import com.github.scheduler.auth.entity.RefreshTokenEntity;
import com.github.scheduler.auth.entity.Role;
import com.github.scheduler.auth.entity.UserEntity;
import com.github.scheduler.auth.entity.UserStatus;
import com.github.scheduler.auth.repository.RefreshTokenRepository;
import com.github.scheduler.auth.repository.UserImageRepository;
import com.github.scheduler.auth.repository.UserRepository;
import com.github.scheduler.global.config.auth.JwtTokenProvider;
import com.github.scheduler.global.config.auth.filter.JwtAuthenticationFilter;
import com.github.scheduler.global.dto.ApiResponse;
import com.github.scheduler.global.exception.AppException;
import com.github.scheduler.global.exception.ErrorCode;
import com.github.scheduler.global.util.PasswordUtil;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.*;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserService {
    private final UserRepository userRepository;
    //private final UserImageRepository userImageRepository;
    private final JwtTokenProvider jwtTokenProvider;
    private final PasswordUtil passwordUtil = new PasswordUtil();
    private final UserImageService userImageService;
    private final JwtAuthenticationFilter jwtAuthenticationFilter;
    private final RefreshTokenRepository refreshTokenRepository;

    @Transactional
    public void signUp(SignUpDto signUpDto, MultipartFile image) throws Exception{
        // 이메일 중복 확인
        if (userRepository.findByEmail(signUpDto.getEmail()).isPresent()) {
            throw new AppException(ErrorCode.USERNAME_DUPLICATED, ErrorCode.USERNAME_DUPLICATED.getMessage());
        }

        // 유저네임 중복 확인
        if (userRepository.findByUsername(signUpDto.getUsername()).isPresent()) {
            throw new AppException(ErrorCode.USERNAME_DUPLICATED, ErrorCode.USERNAME_DUPLICATED.getMessage());
        }

        UserEntity userEntity = UserEntity.builder()
                .email(signUpDto.getEmail())
                .password(passwordUtil.encrypt(signUpDto.getPassword()))
                .username(signUpDto.getUsername())
                .phone(signUpDto.getPhone())
                .role(Role.ROLE_USER)
                .status(UserStatus.ACTIVE)
                .createdAt(LocalDateTime.now())
                .build();

        if (image != null) {
            userImageService.uploadUserImage(userEntity, image);
        }

        userRepository.save(userEntity);
    }

    @Transactional
    public Map<String, String> login(LoginDto loginDto, HttpServletResponse httpServletResponse) {
        UserEntity userEntity = userRepository.findByEmail(loginDto.getEmail())
                .orElseThrow(() -> new AppException(ErrorCode.CHECK_EMAIL_OR_PASSWORD, ErrorCode.CHECK_EMAIL_OR_PASSWORD.getMessage()));

        // 비밀번호 검증
        if (!Objects.equals(passwordUtil.encrypt(loginDto.getPassword()), userEntity.getPassword())) {
            throw new AppException(ErrorCode.NOT_EQUAL_PASSWORD, ErrorCode.NOT_EQUAL_PASSWORD.getMessage());
        }

        // JWT 토큰 생성
        String accessToken = jwtTokenProvider.createAccessToken(userEntity.getEmail());
        String refreshToken = jwtTokenProvider.createRefreshToken(userEntity.getEmail());

        log.info("accessToken: {}", accessToken);
        log.info("refreshToken: {}", refreshToken);

        // 헤더 및 쿠키 설정
        httpServletResponse.addHeader("Authorization", "Bearer " + accessToken);

        Cookie refreshCookie = new Cookie("refresh_token", refreshToken);
        refreshCookie.setHttpOnly(true);
        refreshCookie.setPath("/");
        httpServletResponse.addCookie(refreshCookie);

        addRefresh(loginDto.getEmail(), refreshToken, 10800);

        // 응답 데이터 반환
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

    @Transactional
    public Map<String, String> refreshToken(HttpServletRequest request, HttpServletResponse response) {
        // 쿠키에서 Refresh Token 가져오기
        String refreshToken = findRefreshTokenCookie(request);

        if (refreshToken == null || !jwtTokenProvider.validateToken(refreshToken)) {
            throw new AppException(ErrorCode.INVALID_REFRESH_TOKEN, ErrorCode.INVALID_REFRESH_TOKEN.getMessage());
        }

        // Refresh Token을 기반으로 새로운 Access Token 생성
        String email = jwtTokenProvider.getEmailByToken(refreshToken);
        UserEntity userEntity = userRepository.findByEmail(email)
                .orElseThrow(() -> new AppException(ErrorCode.USER_EMAIL_NOT_FOUND, ErrorCode.USER_EMAIL_NOT_FOUND.getMessage()));

        String newAccessToken = jwtTokenProvider.createAccessToken(email);
        String newRefreshToken = jwtTokenProvider.createRefreshToken(email);
        Role role = userEntity.getRole();

        jwtTokenProvider.setRole(newAccessToken, role.getType());

        // 새로운 Access Token을 응답 헤더에 추가
        response.addHeader("Authorization", "Bearer " + newAccessToken);

        // 새로운 Refresh Token을 쿠키에 추가
        Cookie refreshCookie = new Cookie("refresh_token", newRefreshToken);
        refreshCookie.setHttpOnly(true);
        refreshCookie.setPath("/");
        response.addCookie(refreshCookie);

        addRefresh(email, newRefreshToken, 12 * 60); // 새로운 Refresh Token 저장 (12시간)

        return Map.of(
                "email", email,
                "new_access_token", newAccessToken,
                "new_refresh_token", newRefreshToken,
                "message", "토큰이 갱신되었습니다"
        );
    }

    @Transactional
    public void withdrawalUser(String loginEmail, String requestBodyPassword, HttpSession httpSession) {
        UserEntity userEntity = userRepository.findByEmail(loginEmail)
                .orElseThrow(() -> new AppException(ErrorCode.USER_EMAIL_NOT_FOUND, ErrorCode.USER_EMAIL_NOT_FOUND.getMessage()));

        // 비밀번호 검증
        if (!Objects.equals(passwordUtil.encrypt(requestBodyPassword), userEntity.getPassword())) {
            throw new AppException(ErrorCode.NOT_EQUAL_PASSWORD, ErrorCode.NOT_EQUAL_PASSWORD.getMessage());
        }

        // 회원 삭제
        deleteUser(userEntity);

        // 세션 만료
        httpSession.invalidate();
        log.info("HttpSession invalidated.");

        // Spring Security 컨텍스트 초기화
        SecurityContextHolder.clearContext();
        log.info("SecurityContextHolder cleared.");
        log.info("User account successfully deleted: {}", loginEmail);
    }

    // 회원탈퇴한 유저정보 수정
    private void deleteUser(UserEntity userEntity) {
        LocalDateTime localDateTime = Instant.now().atZone(ZoneId.systemDefault()).toLocalDateTime();

        userEntity.setEmail("");
        userEntity.setPassword("");
        userEntity.setUsername("삭제한 회원");
        userEntity.setPhone("");
        userEntity.setStatus(UserStatus.DELETED);
        userEntity.setDeletedAt(localDateTime);

        userRepository.save(userEntity);
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
        // 만료 시간 설정 (LocalDateTime 사용)
        LocalDateTime expirationDate = LocalDateTime.now().plusMinutes(expiredMinute);

        // 사용자가 존재하는지 확인
        UserEntity userEntity = userRepository.findByEmail(email)
                .orElseThrow(() -> new AppException(ErrorCode.USER_EMAIL_NOT_FOUND, ErrorCode.USER_EMAIL_NOT_FOUND.getMessage()));

        // 기존 리프레시 토큰이 있는 경우 갱신, 없으면 새로 생성
        RefreshTokenEntity refreshTokenEntity = refreshTokenRepository.findByUserEntity(userEntity)
                .orElseGet(() -> new RefreshTokenEntity(userEntity, refreshToken, expirationDate));

        // 리프레시 토큰 및 만료 시간 갱신
        refreshTokenEntity.setRefreshToken(refreshToken);
        refreshTokenEntity.setExpiration(expirationDate);

        // 저장
        refreshTokenRepository.save(refreshTokenEntity);

        log.info("리프레시 토큰이 저장되었습니다. 이메일: {}, 만료 시간: {}", email, expirationDate);
    }
}
