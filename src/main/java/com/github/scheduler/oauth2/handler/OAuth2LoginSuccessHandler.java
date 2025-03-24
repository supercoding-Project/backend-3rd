package com.github.scheduler.oauth2.handler;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.scheduler.auth.entity.UserEntity;
import com.github.scheduler.auth.entity.UserImageEntity;
import com.github.scheduler.auth.repository.UserRepository;
import com.github.scheduler.global.config.auth.JwtTokenProvider;
import com.github.scheduler.global.config.auth.custom.CustomUserDetails;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@Slf4j
@Component
@RequiredArgsConstructor
public class OAuth2LoginSuccessHandler extends SimpleUrlAuthenticationSuccessHandler {
    private final JwtTokenProvider jwtTokenProvider;
    private final UserRepository userRepository;

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response,
                                        Authentication authentication) throws IOException, ServletException {

        CustomUserDetails customUserDetails = (CustomUserDetails) authentication.getPrincipal();
        String accessToken = jwtTokenProvider.createAccessToken(customUserDetails.getUsername());
        String refreshToken = jwtTokenProvider.createRefreshToken(customUserDetails.getUsername());
        UserEntity userEntity = userRepository.findByEmail(authentication.getName())
                .orElseThrow(() -> new RuntimeException("사용자 정보를 찾을 수 없습니다."));

        log.info("OAuth2 로그인 성공: {}", authentication.getName());
        log.info("accessToken: {}", accessToken);
        log.info("refreshToken: {}", refreshToken);

        // 응답 헤더 및 쿠키 설정
        response.addHeader("Authorization", accessToken);
        Cookie refreshCookie = new Cookie("refresh_token", refreshToken);
        refreshCookie.setHttpOnly(true);
        refreshCookie.setPath("/");
        refreshCookie.setMaxAge(60 * 60 * 24 * 7); // 7일
        response.addCookie(refreshCookie);

        // JSON 응답 작성
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");

        Map<String, String> result = new HashMap<>();
        result.put("message", "로그인 되었습니다");
        result.put("token_type", "Bearer");
        result.put("access_token", accessToken);
        result.put("refresh_token", refreshToken);
        result.put("email", authentication.getName());
        result.put("username", userEntity.getUsername());
        result.put("profileImage", Optional.ofNullable(userEntity.getUserImageEntity())
                .map(UserImageEntity::getUrl)
                .orElse("/uploads/profiles/base.png"));

        ObjectMapper objectMapper = new ObjectMapper();
        response.getWriter().write(objectMapper.writeValueAsString(result));
    }
}
