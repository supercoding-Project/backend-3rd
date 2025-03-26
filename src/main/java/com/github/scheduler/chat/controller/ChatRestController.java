package com.github.scheduler.chat.controller;

import com.github.scheduler.auth.entity.UserEntity;
import com.github.scheduler.calendar.dto.CalendarJoinRequestDto;
import com.github.scheduler.chat.dto.*;
import com.github.scheduler.chat.entity.ChatMessage;
import com.github.scheduler.chat.entity.ChatRoom;
import com.github.scheduler.chat.service.ChatRestService;
import com.github.scheduler.chat.service.ChatService;
import com.github.scheduler.global.config.auth.custom.CustomUserDetails;
import com.github.scheduler.global.dto.ApiResponse;
import com.github.scheduler.global.exception.AppException;
import com.github.scheduler.global.exception.ErrorCode;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@RestController
@RequestMapping("/api/v1/chat")
@RequiredArgsConstructor
public class ChatRestController {
    private final ChatRestService chatRestService;

    @Operation(summary = "채팅방 생성")
    @SecurityRequirement(name = "bearerAuth")
    @PostMapping("/room/create/{calendarId}")
    public ResponseEntity<ApiResponse<ChatRoomDto>> createRoom(
            @AuthenticationPrincipal CustomUserDetails customUserDetails,
            @PathVariable Long calendarId) {

        if (customUserDetails == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ApiResponse.fail(ErrorCode.UNAUTHORIZED_ACCESS));
        }
        log.info("채팅방 생성 요청:{}", calendarId);

        return chatRestService.createRoom(calendarId,customUserDetails);
    }
    // roomId 조회
    @Operation(summary = "user id로 채팅방 리스트 조회하기")
    @GetMapping("/rooms")
    public ResponseEntity<ApiResponse<List<ChatRoomUserDto>>> getRooms(
            @AuthenticationPrincipal CustomUserDetails customUserDetails){
        if (customUserDetails == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ApiResponse.fail(ErrorCode.UNAUTHORIZED_ACCESS));
        }
        log.info("채팅방 조회");
        return chatRestService.getChatRooms(customUserDetails);
    }
    // 초대코드 이용해서 채팅방에 참여하기
    @Operation(summary = "캘린더 초대 코드 이용해서 채팅방 참여하기")
    @PostMapping("/room/join")
    public ResponseEntity<ApiResponse<ChatRoomUserDto>> joinChatRoom(
            @AuthenticationPrincipal CustomUserDetails customUserDetails,
            @RequestBody CalendarJoinRequestDto calendarJoinRequestDto) {

        if (customUserDetails == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ApiResponse.fail(ErrorCode.UNAUTHORIZED_ACCESS));
        }

        String inviteCode = calendarJoinRequestDto.getInviteCode();
        if (inviteCode == null || inviteCode.trim().isEmpty() || inviteCode.length() != 8) {
            throw new AppException(ErrorCode.INVALID_INVITE_CODE, ErrorCode.INVALID_INVITE_CODE.getMessage());
        }

        log.info("초대 코드 입력 - 사용자: {}, 코드: {}", customUserDetails.getUsername(), inviteCode);
        return chatRestService.joinRoom(customUserDetails,inviteCode);
    }

    @Operation(summary = "user가 마지막으로 읽은 메시지 id update")
    @PatchMapping("/rooms/{roomId}/last-read")
    public ResponseEntity<ApiResponse<Void>> updateLastReadMessage(
            @AuthenticationPrincipal CustomUserDetails customUserDetails,
            @PathVariable Long roomId) {
        if (customUserDetails == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ApiResponse.fail(ErrorCode.UNAUTHORIZED_ACCESS));
        }
        // user가 채팅방을 나가거나 새로고침할 때, 마지막으로 읽은 메시지를 저장
        chatRestService.updateLastReadMessage(roomId,customUserDetails);
        return ResponseEntity.ok().build();
    }
    // todo
    // 메시지 조회
    // userid, roomid , message ,
    @Operation(summary = "채팅 메시지 조회",
            description = "유저가 채팅방에 join한 시점 이후로만 조회가 가능, 안읽은 메시지 + 조회하고 싶은 기간 으로 조회")
    @PostMapping("/message")
    public ResponseEntity<ApiResponse<List<ChatMessageDto>>> sendMessage(
            @AuthenticationPrincipal CustomUserDetails customUserDetails,
            @RequestBody ChatMessageGetRequest chatMessageGetRequest) {
        if (customUserDetails == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ApiResponse.fail(ErrorCode.UNAUTHORIZED_ACCESS));
        }
        log.info("메시지 조회할 채팅방: {}",chatMessageGetRequest.getRoomId());

        return chatRestService.getMessage(customUserDetails,chatMessageGetRequest);
    }





}
