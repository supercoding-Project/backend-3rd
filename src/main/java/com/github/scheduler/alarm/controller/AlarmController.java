package com.github.scheduler.alarm.controller;

import com.github.scheduler.alarm.dto.SchedulerAlarmDto;
import com.github.scheduler.alarm.service.AlarmService;
import com.github.scheduler.global.config.auth.custom.CustomUserDetails;
import com.github.scheduler.global.dto.ApiResponse;
import com.github.scheduler.mypage.dto.UserDto;
import com.github.scheduler.mypage.service.MyPageService;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.repository.query.Param;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

@Slf4j
@RestController
@RequestMapping("/api/alarm")
//@RequestMapping("/api/v1/alarm")
@RequiredArgsConstructor
public class AlarmController {

    private final AlarmService alarmService;

    @Operation(summary = "스케줄 알람 전송", description = "사용자 스케줄에 맞는 알람을 전송합니다.")
    @GetMapping("/schedule")  // 경로 수정
    public ResponseEntity<ApiResponse<String>> sendAlarms(@RequestParam Long userId) {
        alarmService.checkAndSendScheduleAlarms(userId);
        return ResponseEntity.ok(ApiResponse.success("알림이 성공적으로 전송되었습니다."));
    }
//    @Operation(summary = "스케줄 알람 정보 조회", description = "")
//    @GetMapping("/schedule/{email}")
//    public ResponseEntity<ApiResponse<String>> myScheduleAlarm(
//             //@AuthenticationPrincipal CustomUserDetails customUserDetails,
//             @PathVariable("email") String email) {
//            Long userId = 1L;
////        if (customUserDetails == null) {
////            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED,"인증되지 않은 사용자입니다.");
////        }
////        if (!customUserDetails.getUserEntity().getEmail().equals(email)) {
////            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "본인의 정보만 조회할 수 있습니다.");
////        }
//        alarmService.checkAndSendScheduleAlarms(userId);
//        return ResponseEntity.ok(ApiResponse.success("일정이 생성되었습니다."));
//    }
//    @Operation(summary = "Q&A 알람 정보 조회", description = "")
//    @GetMapping("/qna")
//    public void myQnAlarm() {
//
//    }
//    @Operation(summary = "공지사항 알람 정보 조회", description = "")
//    @GetMapping("/notice")
//    public void myNoticeAlarm() {

//    }
    @Operation(summary = "읽지않은 알람 정보 조회", description = "")
    @GetMapping
    public void myAlarmCount() {

    }
}
