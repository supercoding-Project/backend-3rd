package com.github.scheduler.schedule.controller;

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
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("api/v1/schedules")
@RequiredArgsConstructor
public class ScheduleController {

    private final ScheduleService scheduleService;

    // 일정 조회 (월별, 주별, 일별)
    @Operation(summary = "일정 조회: 월별, 주별, 일별 조회")
    @GetMapping
    public ResponseEntity<ApiResponse<List<ScheduleDto>>> getSchedules(
            @AuthenticationPrincipal CustomUserDetails customUserDetails,
            @RequestParam(name = "view", defaultValue = "monthly") String view,
            @RequestParam(name = "date") String date,
            @RequestParam(name = "scheduleType", defaultValue = "both") String scheduleType) {

        if (customUserDetails == null) {
            throw new AppException(ErrorCode.NOT_AUTHORIZED_USER, ErrorCode.NOT_AUTHORIZED_USER.getMessage());
        }

        List<ScheduleDto> schedule = scheduleService.getSchedules(customUserDetails, view, date, scheduleType);
        return ResponseEntity.ok(ApiResponse.success(schedule));
    }

    // 일정 등록
    @Operation(summary = "개인 또는 팀 일정 등록")
    @PostMapping
    public ResponseEntity<ApiResponse<List<CreateScheduleDto>>> createSchedule(
            @AuthenticationPrincipal CustomUserDetails customUserDetails,
            @RequestBody CreateScheduleDto createScheduleDto) {

        if (customUserDetails == null) {
            throw new AppException(ErrorCode.NOT_AUTHORIZED_USER, ErrorCode.NOT_AUTHORIZED_USER.getMessage());
        }
        List<CreateScheduleDto> createdSchedule = scheduleService.createSchedule(customUserDetails, createScheduleDto);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success(createdSchedule));
    }

    // 일정 수정
    @Operation(summary = "개인 또는 팀 일정 수정")
    @PutMapping("/{scheduleId}")
    public ResponseEntity<ApiResponse<List<UpdateScheduleDto>>> updateSchedule(
            @AuthenticationPrincipal CustomUserDetails customUserDetails,
            @RequestBody UpdateScheduleDto updateScheduleDto,
            @PathVariable Long scheduleId,
            @RequestParam(name = "scheduleType", defaultValue = "both") String scheduleType) {

        if (customUserDetails == null) {
            throw new AppException(ErrorCode.NOT_AUTHORIZED_USER, ErrorCode.NOT_AUTHORIZED_USER.getMessage());
        }
        List<UpdateScheduleDto> updatedSchedule = scheduleService.updateSchedule(customUserDetails, updateScheduleDto, scheduleId, scheduleType);
        return ResponseEntity.ok(ApiResponse.success(updatedSchedule));
    }

    // 일정 삭제
    @Operation(summary = "개인 또는 팀 일정 삭제")
    @DeleteMapping("/{scheduleId}")
    public ResponseEntity<ApiResponse<DeleteScheduleDto>> deleteSchedule(
            @AuthenticationPrincipal CustomUserDetails customUserDetails,
            @RequestBody DeleteScheduleDto deleteScheduleDto,
            @PathVariable("scheduleId") Long scheduleId) {

        if (customUserDetails == null) {
            throw new AppException(ErrorCode.NOT_AUTHORIZED_USER, ErrorCode.NOT_AUTHORIZED_USER.getMessage());
        }
        DeleteScheduleDto deletedSchedule = scheduleService.deleteSchedule(customUserDetails, deleteScheduleDto,scheduleId);
        return ResponseEntity.ok(ApiResponse.success(deletedSchedule));
    }
}
