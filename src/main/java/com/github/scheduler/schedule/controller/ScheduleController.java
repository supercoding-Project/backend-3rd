package com.github.scheduler.schedule.controller;

import com.github.scheduler.calendar.entity.CalendarType;
import com.github.scheduler.global.config.auth.custom.CustomUserDetails;
import com.github.scheduler.global.dto.ApiResponse;
import com.github.scheduler.global.exception.AppException;
import com.github.scheduler.global.exception.ErrorCode;
import com.github.scheduler.schedule.dto.CreateScheduleDto;
import com.github.scheduler.schedule.dto.DeleteScheduleDto;
import com.github.scheduler.schedule.dto.ScheduleDto;
import com.github.scheduler.schedule.dto.UpdateScheduleDto;
import com.github.scheduler.schedule.service.ScheduleService;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.Collections;
import java.util.List;

@Slf4j
@RestController
@RequestMapping("api/v1/schedules")
@RequiredArgsConstructor
public class ScheduleController {

    private final ScheduleService scheduleService;

    @Operation(summary = "일정 조회: 월별, 주별, 일별 조회", description = "사용자의 일정을 조회합니다. 일정 조회 시 할 일 정보가 함께 조회될 수 있습니다.")
    @GetMapping
    public ResponseEntity<ApiResponse<List<ScheduleDto>>> getSchedules(
            @AuthenticationPrincipal CustomUserDetails customUserDetails,
            @RequestParam(name = "view", defaultValue = "MONTHLY") String view,
            @RequestParam(name = "date") String date,
            @RequestParam(name = "calendarTypes", required = false) List<CalendarType> calendarTypes) {

        if (customUserDetails == null) {
            throw new AppException(ErrorCode.NOT_AUTHORIZED_USER, ErrorCode.NOT_AUTHORIZED_USER.getMessage());
        }

        // 선택된 타입이 없으면 기본으로 PERSONAL 을 사용
        if (calendarTypes == null || calendarTypes.isEmpty()) {
            calendarTypes = Collections.singletonList(CalendarType.PERSONAL);
        }

        List<ScheduleDto> schedule = scheduleService.getSchedules(customUserDetails, view, date, calendarTypes);
        return ResponseEntity.ok(ApiResponse.success(schedule));
    }

    // 일정 등록
    @Operation(summary = "개인 또는 팀 일정 등록", description = "개인 또는 팀 일정을 등록합니다.")
    @PostMapping
    public ResponseEntity<ApiResponse<List<CreateScheduleDto>>> createSchedule(
            @AuthenticationPrincipal CustomUserDetails customUserDetails,
            @Valid @RequestBody CreateScheduleDto createScheduleDto,
            @RequestParam(name = "calendarType", defaultValue = "PERSONAL") CalendarType calendarType,
            @RequestParam(name = "calendarId") Long calendarId) {

        if (customUserDetails == null) {
            throw new AppException(ErrorCode.NOT_AUTHORIZED_USER, ErrorCode.NOT_AUTHORIZED_USER.getMessage());
        }
        List<CreateScheduleDto> createdSchedule = scheduleService.createSchedule(customUserDetails, createScheduleDto, calendarType, calendarId);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success(createdSchedule));
    }

    // 일정 수정 (개인 또는 팀 일정 수정)
    @Operation(summary = "일정 수정", description = "개인 또는 팀 일정을 수정합니다.")
    @PutMapping("/{scheduleId}")
    public ResponseEntity<ApiResponse<List<CreateScheduleDto>>> updateSchedule(
            @AuthenticationPrincipal CustomUserDetails customUserDetails,
            @Valid @RequestBody CreateScheduleDto updateScheduleDto,
            @PathVariable Long scheduleId,
            @RequestParam(name = "calendarType", defaultValue = "SHARED") CalendarType calendarType) {

        if (customUserDetails == null) {
            throw new AppException(ErrorCode.NOT_AUTHORIZED_USER, ErrorCode.NOT_AUTHORIZED_USER.getMessage());
        }
        List<CreateScheduleDto> updatedSchedules = scheduleService.updateSchedule(customUserDetails, updateScheduleDto, scheduleId, calendarType);
        return ResponseEntity.ok(ApiResponse.success(updatedSchedules));
    }

    // 일정 삭제 (개인 또는 팀 일정 삭제)
    @Operation(summary = "일정 삭제", description = "개인 또는 팀 일정을 삭제합니다.")
    @DeleteMapping("/{scheduleId}")
    public ResponseEntity<ApiResponse<DeleteScheduleDto>> deleteSchedule(
            @AuthenticationPrincipal CustomUserDetails customUserDetails,
            @Valid @RequestBody DeleteScheduleDto deleteScheduleDto,
            @PathVariable("scheduleId") Long scheduleId,
            @RequestParam(name = "calendarType", defaultValue = "PERSONAL") CalendarType calendarType) {

        if (customUserDetails == null) {
            throw new AppException(ErrorCode.NOT_AUTHORIZED_USER, ErrorCode.NOT_AUTHORIZED_USER.getMessage());
        }
        DeleteScheduleDto deletedSchedule = scheduleService.deleteSchedule(customUserDetails, deleteScheduleDto, scheduleId, calendarType);
        return ResponseEntity.ok(ApiResponse.success(deletedSchedule));
    }
}
