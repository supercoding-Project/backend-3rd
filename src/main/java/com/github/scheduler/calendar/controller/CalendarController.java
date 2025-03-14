package com.github.scheduler.calendar.controller;

import com.github.scheduler.calendar.dto.CalendarRequestDto;
import com.github.scheduler.calendar.dto.CalendarResponseDto;
import com.github.scheduler.calendar.service.CalendarService;
import com.github.scheduler.global.config.auth.custom.CustomUserDetails;
import com.github.scheduler.global.dto.ApiResponse;
import com.github.scheduler.global.exception.ErrorCode;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RequiredArgsConstructor
@RestController
@RequestMapping("/api")
public class CalendarController {
    private final CalendarService calendarService;

    @Operation(summary = "캘린더 생성하기", description = "캘린더 생성할 때 개인, 공용, 할일 중 선택")
    @PostMapping("/v1/create-calendar")
    public ResponseEntity<ApiResponse<CalendarResponseDto>> addCalendar(
            @RequestBody @Valid CalendarRequestDto calendarRequestDto,
            BindingResult bindingResult,
            @AuthenticationPrincipal CustomUserDetails customUserDetails) {

        if (customUserDetails == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ApiResponse.fail(ErrorCode.UNAUTHORIZED_ACCESS));
        }

        String email = customUserDetails.getUsername();

        log.info("POST 요청 - 사용자: {}의 캘린더 생성 요청", email);

        if (bindingResult.hasErrors()) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.fail(ErrorCode.BINDING_RESULT_ERROR));
        }

        return ResponseEntity.ok(ApiResponse.success(calendarService.createCalendar(calendarRequestDto, email)));
    }
}
