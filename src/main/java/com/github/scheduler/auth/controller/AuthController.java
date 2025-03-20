package com.github.scheduler.auth.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.scheduler.auth.dto.EmailRequestDto;
import com.github.scheduler.auth.dto.LoginDto;
import com.github.scheduler.auth.dto.SignUpDto;
import com.github.scheduler.auth.service.UserService;
import com.github.scheduler.global.config.auth.custom.CustomUserDetails;
import com.github.scheduler.global.dto.ApiResponse;
import com.github.scheduler.global.exception.AppException;
import com.github.scheduler.global.exception.ErrorCode;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
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
    private final ObjectMapper objectMapper;

    @Operation(summary = "회원가입", description = "JSON 데이터(dto)와 이미지 파일(image)을 함께 업로드하는 회원가입 API")
    @PostMapping(value = "/signup", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ApiResponse<String>> signUp(
            @Parameter(
                    description = "회원가입 정보(JSON 형식)",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = SignUpDto.class)
                    )
            )
            @RequestPart(value = "dto") String dtoJson,  // JSON을 문자열로 받음

            @Parameter(
                    description = "회원 프로필 이미지 파일",
                    content = @Content(
                            mediaType = "image/png",
                            schema = @Schema(type = "string", format = "binary")
                    )
            )
            @RequestPart(value = "image", required = false) MultipartFile image, // 이미지 파일 (선택)

            BindingResult bindingResult) throws Exception {

        log.info("[POST]: 회원가입 요청");

        // 🔥 JSON 문자열을 SignUpDto 객체로 변환
        SignUpDto signUpDto = objectMapper.readValue(dtoJson, SignUpDto.class);

        if (bindingResult.hasErrors()) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.fail(ErrorCode.BINDING_RESULT_ERROR));
        }

        userService.signUp(signUpDto, image);

        return ResponseEntity.ok(ApiResponse.success("회원가입이 완료되었습니다."));
    }

    @Operation(summary = "이메일 중복 체크", description = "이메일 중복 확인하는 API 입니다.")
    @PostMapping("/check-email")
    public ResponseEntity<ApiResponse<String>> checkEmail(@RequestBody EmailRequestDto emailRequest) {
        boolean isAvailable = userService.isEmailAvailable(emailRequest.getEmail());

        if (isAvailable) {
            return ResponseEntity.ok(ApiResponse.success("사용 가능한 이메일입니다."));
        } else {
            return ResponseEntity.ok(ApiResponse.success("중복된 이메일입니다."));
        }
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
