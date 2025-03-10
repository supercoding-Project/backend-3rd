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
import com.github.scheduler.global.exception.AppException;
import com.github.scheduler.global.exception.ErrorCode;
import com.github.scheduler.global.util.PasswordUtil;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.security.NoSuchAlgorithmException;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.*;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserService {
    private final UserRepository userRepository;
    private final UserImageRepository userImageRepository;
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
    public ResponseEntity<?> login(LoginDto loginDto, HttpServletResponse httpServletResponse) {
        UserEntity userEntity = userRepository.findByEmail(loginDto.getEmail()).isEmpty() ? null : userRepository.findByEmail(loginDto.getEmail()).get();

        if (userEntity == null) {
            throw new AppException(ErrorCode.CHECK_EMAIL_OR_PASSWORD, ErrorCode.CHECK_EMAIL_OR_PASSWORD.getMessage());
        }

        Map<String, String> response = new HashMap<>();

        String accessToken = "";
        String refreshToken = "";

        try {
            UsernamePasswordAuthenticationToken authenticationToken = new UsernamePasswordAuthenticationToken(loginDto.getEmail(), loginDto.getPassword());

            if (!Objects.equals(passwordUtil.encrypt(loginDto.getPassword()), userEntity.getPassword())) {
                throw new AppException(ErrorCode.NOT_EQUAL_PASSWORD, ErrorCode.NOT_EQUAL_PASSWORD.getMessage());
            }

            accessToken = jwtTokenProvider.createAccessToken(authenticationToken.getName());
            refreshToken = jwtTokenProvider.createRefreshToken(authenticationToken.getName());

            log.info(authenticationToken.getName());
            log.info("accessToken: {}", accessToken);
            log.info("refreshToken: {}", refreshToken);

            httpServletResponse.addHeader("Authorization", accessToken);
            httpServletResponse.addCookie(new Cookie("refresh_token", refreshToken));

            addRefresh(loginDto.getEmail(), refreshToken, 10800);

            if (userEntity.getRole() != null) {
                response.put("isAuth", "true");
            } else {
                response.put("isAuth", "false");
            };

            response.put("message", "로그인 되었습니다");
            response.put("token_type", "Bearer");
            response.put("access_token", accessToken);
            response.put("refresh_token", refreshToken);
            response.put("email", userEntity.getEmail());
            response.put("username", userEntity.getUsername());

            return ResponseEntity.status(HttpStatus.OK).body(response);

        } catch (BadCredentialsException e) {
            log.error("BadCredentialsException: {}", e.getMessage());
            response.put("message", "잘못된 자격 증명입니다");
            response.put("http_status", HttpStatus.UNAUTHORIZED.toString());
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(response);

        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }
    }

    @Transactional
    public ResponseEntity<?> refreshToken(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse) {
        // 쿠키에 있는 Refresh Token 가져오기
        String refreshToken = findRefreshTokenCookie(httpServletRequest);

        // 새로운 Access Token 생성
        String email = jwtTokenProvider.getEmailByToken(refreshToken);
        UserEntity userEntity = userRepository.findByEmail(email).orElseThrow(
                () -> new AppException(ErrorCode.USER_EMAIL_NOT_FOUND, ErrorCode.USER_EMAIL_NOT_FOUND.getMessage())
        );

        String newAccessToken = jwtTokenProvider.createAccessToken(email);
        String newRefreshToken  = jwtTokenProvider.createRefreshToken(email);
        Role role = userEntity.getRole();

        jwtTokenProvider.setRole(newAccessToken, role.getType());
        httpServletResponse.addHeader("Authorization", newAccessToken);
        httpServletResponse.addCookie(new Cookie("refresh_token", newRefreshToken));


        addRefresh(email, newRefreshToken, 12*60);
        Map<String, String> response = new HashMap<>();

        response.put("유저 email: ", email);
        response.put("새로운 AccessToken: ", newAccessToken);
        response.put("새로운 RefreshToken: ", newRefreshToken);

        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    public void withdrawalUser(String loginEmail, String requestBodyPassword, HttpSession httpSession) throws NoSuchAlgorithmException {
        UserEntity userEntity = userRepository.findByEmail(loginEmail).orElseThrow(
                () -> new AppException(ErrorCode.USER_EMAIL_NOT_FOUND, ErrorCode.USER_EMAIL_NOT_FOUND.getMessage())
        );

        String encodedPassword = passwordUtil.encrypt(requestBodyPassword);
        String userEntityPassword = userEntity.getPassword();

        if (!Objects.equals(encodedPassword, userEntityPassword)) {
            throw new AppException(ErrorCode.NOT_EQUAL_PASSWORD, ErrorCode.NOT_EQUAL_PASSWORD.getMessage());
        } else {
            deleteUser(userEntity);
            httpSession.invalidate();
            log.info("httpSession: {}", httpSession);
            SecurityContextHolder.clearContext();
            log.info("SecurityContextHolder: {}", SecurityContextHolder.getContext());
            log.info("User account successfully deleted: {}", loginEmail);
        }
    }

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

    public String findRefreshTokenCookie(HttpServletRequest request) {
        Cookie[] cookies = request.getCookies();
        if (cookies != null) {
            for (Cookie cookie : cookies) {
                if ("refresh_token".equals(cookie.getName())) {
                    return cookie.getValue();
                }

                if (cookie.getValue() == null) {
                    throw new AppException(ErrorCode.NOT_FOUND_REFRESH_TOKEN, ErrorCode.NOT_FOUND_REFRESH_TOKEN.getMessage());
                }
                return cookie.getValue();
            }

        } else {
            throw new AppException(ErrorCode.NOT_FOUND_COOKIE, ErrorCode.NOT_FOUND_COOKIE.getMessage());
        }
        return null;
    }

    protected void addRefresh(String email, String refreshToken, int expiredMinute) {
        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.MINUTE, expiredMinute); // 만료 시간 설정
        Date expirationDate = calendar.getTime();

        // 사용자가 존재하는지 확인
        UserEntity userEntity = userRepository.findByEmail(email).orElseThrow(
                () -> new AppException(ErrorCode.USER_EMAIL_NOT_FOUND, ErrorCode.USER_EMAIL_NOT_FOUND.getMessage())
        );

        // 기존 리프레시 토큰이 존재하는지 확인
        Optional<RefreshTokenEntity> existingToken = refreshTokenRepository.findByUserEntity(userEntity);

        if (existingToken.isPresent()) {
            // 기존 토큰이 존재하면 갱신
            RefreshTokenEntity tokenEntity = existingToken.get();
            tokenEntity.setRefreshToken(refreshToken); // 기존 토큰 덮어쓰기
            tokenEntity.setExpiration(expirationDate.toString()); // 만료 시간 갱신

            refreshTokenRepository.save(tokenEntity);
        } else {
            // 기존 토큰이 없으면 새로 추가
            RefreshTokenEntity refreshTokenEntity = new RefreshTokenEntity();
            refreshTokenEntity.setUserEntity(userEntity);
            refreshTokenEntity.setRefreshToken(refreshToken);
            refreshTokenEntity.setExpiration(expirationDate.toString());

            refreshTokenRepository.save(refreshTokenEntity);
        }
    }
}
