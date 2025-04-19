package com.github.scheduler.alarm.controller;

import com.corundumstudio.socketio.SocketIOServer;
import com.github.scheduler.alarm.dto.ResponseAlarmDto;
import com.github.scheduler.alarm.dto.SchedulerAlarmDto;
import com.github.scheduler.alarm.service.AlarmService;
import com.github.scheduler.chat.dto.ChatMessageRequest;
import com.github.scheduler.global.config.alarm.AlarmSocketInterceptor;
import com.github.scheduler.global.config.auth.custom.CustomUserDetails;
import com.github.scheduler.global.config.chat.SocketSecurityInterceptor;
import com.github.scheduler.global.dto.ApiResponse;
import com.github.scheduler.global.exception.ErrorCode;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Set;

@Slf4j
@RestController
@RequestMapping("/api/v1/alarms")
public class AlarmController {

    private final AlarmService alarmService;

    private final SocketIOServer server;

//    @Qualifier("alarmSocketServer")
//    private final SocketIOServer server;
    private final AlarmSocketInterceptor securityInterceptor;


    public AlarmController(
            AlarmService alarmService,
            @Qualifier("alarmSocketServer") SocketIOServer socketIOServer,
            AlarmSocketInterceptor securityInterceptor) {
        this.alarmService = alarmService;
        this.server = socketIOServer;
        this.securityInterceptor = securityInterceptor;
    }
//    @MessageMapping("/queue/alarms")
//    public void
    @PostConstruct
    public void init(){
        server.start();
        // 연결,해제 이벤트 리스너
        server.addConnectListener(securityInterceptor);
        // 메시지 이벤트 리스너
        server.addEventListener("sendAlarm", ResponseAlarmDto.class , alarmService::sendAlarmToClient);
    }

    @PreDestroy
    public void stopServer() {
        server.stop();

    }

    @PutMapping("/{alarmId}")
    @Operation(summary = "알림 읽음", description = "알림을 읽음처리합니다.")
    public ResponseEntity<ApiResponse<ResponseAlarmDto>> markAlarmAsRead(
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
    public ResponseEntity<ApiResponse<List<ResponseAlarmDto>>> getUnreadAlarms(
            @AuthenticationPrincipal CustomUserDetails customUserDetails) {
        if (customUserDetails == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ApiResponse.fail(ErrorCode.UNAUTHORIZED_ACCESS));
        }
        Long userId = customUserDetails.getUserEntity().getUserId();
        List<ResponseAlarmDto> unreadAlarms = alarmService.getUnreadAlarms(userId);
        return ResponseEntity.ok(ApiResponse.success(unreadAlarms));
    }

    @PutMapping("/all")
    @Operation(summary = "알림 전체 읽음 처리", description = "사용자의 모든 읽지 않은 알림을 읽음 처리합니다.")
    public ResponseEntity<ApiResponse<List<ResponseAlarmDto>>> markAllAlarmsAsRead(
            @AuthenticationPrincipal CustomUserDetails customUserDetails
    ) {
        if (customUserDetails == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ApiResponse.fail(ErrorCode.UNAUTHORIZED_ACCESS));
        }

        Long userId = customUserDetails.getUserEntity().getUserId();
        List<ResponseAlarmDto> updatedAlarms = alarmService.markAllAlarmsAsRead(userId);
        return ResponseEntity.ok(ApiResponse.success(updatedAlarms));
    }


    @GetMapping
    @Operation(summary = "읽지않은 알림 개수", description = "읽지않은 알림의 전체 개수를 조회한다.")
    public ResponseEntity<ApiResponse<Long>> alarmCount(@AuthenticationPrincipal CustomUserDetails customUserDetails) {
        if (customUserDetails == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ApiResponse.fail(ErrorCode.UNAUTHORIZED_ACCESS));
        }
        Long userId = customUserDetails.getUserEntity().getUserId();
        long alarmCount = alarmService.alarmCount(userId);
        return ResponseEntity.ok(ApiResponse.success(alarmCount));
    }

}
