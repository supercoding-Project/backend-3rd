package com.github.scheduler.alarm.controller;

import com.github.scheduler.alarm.dto.SchedulerAlarmDto;
import com.github.scheduler.alarm.service.AlarmService;
import com.github.scheduler.global.config.auth.custom.CustomUserDetails;
import com.github.scheduler.global.dto.ApiResponse;
import com.github.scheduler.global.exception.ErrorCode;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Set;

@Slf4j
@RestController
@RequestMapping("/api/v1/alarms")
@RequiredArgsConstructor
public class AlarmController {

    private final AlarmService alarmService;

    @PatchMapping("/{alarmId}")
    @Operation(summary = "알림 읽음", description = "알림을 읽음처리합니다.")
    public ResponseEntity<ApiResponse<SchedulerAlarmDto>> markAlarmAsRead(
            @AuthenticationPrincipal CustomUserDetails customUserDetails,
            @PathVariable Long alarmId,
            @RequestParam String alarmType
    ) {
        if (customUserDetails == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ApiResponse.fail(ErrorCode.UNAUTHORIZED_ACCESS));
        }
        if (alarmType.equalsIgnoreCase("member_added") || alarmType.equalsIgnoreCase("member_invited")) {
            alarmType = "invitation";
        }else{
            alarmType = "schedule";
        }

        Long userId = customUserDetails.getUserEntity().getUserId();
        return ResponseEntity.ok(ApiResponse.success(alarmService.markAlarmAsRead(userId, alarmId, alarmType)));
    }

    @Operation(summary = "알림 전체조회", description = "읽지않은 알림을 전체조회합니다.")
    @GetMapping("/unread")
    public ResponseEntity<ApiResponse<List<SchedulerAlarmDto>>> getUnreadAlarms(
            @AuthenticationPrincipal CustomUserDetails customUserDetails) {
        if (customUserDetails == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ApiResponse.fail(ErrorCode.UNAUTHORIZED_ACCESS));
        }
        Long userId = customUserDetails.getUserEntity().getUserId();
        List<SchedulerAlarmDto> unreadAlarms = alarmService.getUnreadAlarms(userId);
        return ResponseEntity.ok(ApiResponse.success(unreadAlarms));
    }
}
