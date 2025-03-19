package com.github.scheduler.auth.controller;

import com.github.scheduler.auth.dto.LoginDto;
import com.github.scheduler.auth.dto.SignUpDto;
import com.github.scheduler.auth.service.UserService;
import com.github.scheduler.global.config.auth.custom.CustomUserDetails;
import com.github.scheduler.global.dto.ApiResponse;
import com.github.scheduler.global.exception.AppException;
import com.github.scheduler.global.exception.ErrorCode;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.validation.BindingResult;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class AuthController {

    private final UserService userService;
    private final PasswordEncoder passwordEncoder;

    // 회원가입
    @Operation(summary = "유저 회원가입", description = "회원가입 api 입니다.")
    @PostMapping("/signup")
    public ResponseEntity<ApiResponse<String>> signUp(
            @Validated @RequestPart(value = "dto") SignUpDto signUpDto,
            @RequestPart(value = "image") MultipartFile image,
            BindingResult bindingResult) throws Exception {

        log.info("[POST]: 회원가입 요청");

        // 유효성 검사 실패 시
        if (bindingResult.hasErrors()) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.fail(ErrorCode.BINDING_RESULT_ERROR));
        }

        userService.signUp(signUpDto, image);

        return ResponseEntity.ok(ApiResponse.success("회원가입이 완료되었습니다."));
    }

    @Operation(summary = "유저 로그인", description = "로그인 API 입니다.")
    @PostMapping("/login")
    public ResponseEntity<ApiResponse<Map<String, String>>> login(
            @RequestBody LoginDto loginDto,
            HttpServletResponse httpServletResponse) {

        log.info("[POST]: 로그인 요청");

        try {
            // 로그인 서비스 호출 → 토큰 정보 반환
            Map<String, String> tokenResponse = userService.login(loginDto, httpServletResponse);

            return ResponseEntity.ok(ApiResponse.success(tokenResponse));

        } catch (AppException e) {
            return ResponseEntity.status(e.getErrorCode().getHttpStatus())
                    .body(ApiResponse.fail(e.getErrorCode()));
        }
    }

    @Operation(summary = "리프레시 토큰 갱신", description = "리프레시 토큰을 사용하여 새로운 액세스 토큰을 발급합니다.")
    @PostMapping("/refresh")
    public ResponseEntity<ApiResponse<Map<String, String>>> refreshToken(
            HttpServletRequest request, HttpServletResponse response) {

        log.info("[POST]: 리프레시 토큰 요청");

        try {
            // 서비스 호출 → 새로운 토큰 정보 반환
            Map<String, String> tokenResponse = userService.refreshToken(request, response);

            return ResponseEntity.ok(ApiResponse.success(tokenResponse));

        } catch (AppException e) {
            return ResponseEntity.status(e.getErrorCode().getHttpStatus())
                    .body(ApiResponse.fail(e.getErrorCode()));
        }
    }

    @Operation(summary = "회원탈퇴", description = "회원탈퇴 API 입니다.")
    @PutMapping("/v1/withdrawal")
    public ResponseEntity<ApiResponse<String>> withdrawalUser(
            @RequestBody Map<String, String> passwordMap,
            @AuthenticationPrincipal CustomUserDetails customUserDetails,
            HttpSession httpSession) {

        log.info("[PUT]: 회원탈퇴 요청 - {}", customUserDetails.getUsername());

        try {
            String loginEmail = customUserDetails.getUsername();
            String requestBodyPassword = passwordMap.get("password");

            userService.withdrawalUser(loginEmail, requestBodyPassword, httpSession);

            return ResponseEntity.ok(ApiResponse.success("회원탈퇴가 완료되었습니다."));

        } catch (AppException e) {
            return ResponseEntity.status(e.getErrorCode().getHttpStatus())
                    .body(ApiResponse.fail(e.getErrorCode()));
        }
    }
}
