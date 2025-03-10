package com.github.scheduler.auth.controller;

import com.github.scheduler.auth.dto.LoginDto;
import com.github.scheduler.auth.dto.SignUpDto;
import com.github.scheduler.auth.service.UserService;
import com.github.scheduler.global.config.auth.custom.CustomUserDetails;
import com.github.scheduler.global.dto.MsgResponseDto;
import com.github.scheduler.global.exception.ErrorCode;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.security.NoSuchAlgorithmException;
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
    public ResponseEntity<?> signUp(@Valid @RequestPart(value = "dto") SignUpDto signUpDto, @RequestPart(value = "image") MultipartFile image, BindingResult bindingResult) throws Exception {
        log.info("[POST]: 회원가입 요청");

        if (bindingResult.hasErrors()) {
            return ResponseEntity.badRequest().body(ErrorCode.BINDING_RESULT_ERROR.getMessage());
        }

        userService.signUp(signUpDto, image);

        return ResponseEntity.ok(new MsgResponseDto("회원가입이 완료되었습니다.", HttpStatus.OK.value()));
    }

    // 로그인
    @Operation(summary = "유저 로그인", description = "로그인 api 입니다.")
    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginDto loginDto, HttpServletResponse httpServletResponse) {
        log.info("[POST]: 로그인 요청");

        return userService.login(loginDto, httpServletResponse);
    }

    // refresh 토큰
    @PostMapping("/refresh")
    public ResponseEntity<?> refreshToken(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse) {

        return userService.refreshToken(httpServletRequest, httpServletResponse);
    }

    // 회원탈퇴
    @Operation(summary = "회원탈퇴", description = "회원탈퇴 api 입니다.")
    @PutMapping("/v1/withdrawal")
    public ResponseEntity<?> withdrawalUser(@RequestBody Map<String, String> passwordMap, @AuthenticationPrincipal CustomUserDetails customUserDetails, HttpSession httpSession) throws NoSuchAlgorithmException {
        String loginEmail = customUserDetails.getUsername();
        String requestBodyPassword = passwordMap.get("password");

        userService.withdrawalUser(loginEmail, requestBodyPassword, httpSession);
        return ResponseEntity.ok(new MsgResponseDto("회원탈퇴가 완료되었습니다.", HttpStatus.OK.value()));
    }
}
