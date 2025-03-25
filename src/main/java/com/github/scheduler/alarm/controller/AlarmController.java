package com.github.scheduler.alarm.controller;

import com.corundumstudio.socketio.SocketIOClient;
import com.github.scheduler.alarm.dto.SchedulerAlarmDto;
import com.github.scheduler.alarm.service.AlarmService;
import com.github.scheduler.global.config.alarm.WebSocketSessionManager;
import com.github.scheduler.global.config.auth.custom.CustomUserDetails;
import com.github.scheduler.global.dto.ApiResponse;
import com.github.scheduler.mypage.dto.UserDto;
import com.github.scheduler.mypage.service.MyPageService;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.repository.query.Param;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.Set;

@Slf4j
@Controller
@RequiredArgsConstructor
public class AlarmController {


}
