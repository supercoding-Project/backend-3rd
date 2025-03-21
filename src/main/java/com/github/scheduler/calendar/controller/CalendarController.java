package com.github.scheduler.calendar.controller;

import com.github.scheduler.calendar.dto.*;
import com.github.scheduler.calendar.service.CalendarService;
import com.github.scheduler.global.config.auth.custom.CustomUserDetails;
import com.github.scheduler.global.dto.ApiResponse;
import com.github.scheduler.global.exception.AppException;
import com.github.scheduler.global.exception.ErrorCode;
import com.github.scheduler.invite.dto.InviteRequestDto;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
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
    @SecurityRequirement(name = "bearerAuth")
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

    @Operation(summary = "초대 코드 이메일 전송", description = "여러 명에게 초대 코드를 이메일로 전송")
    @SecurityRequirement(name = "bearerAuth")
    @PostMapping("/v1/calendar/{calendarId}/send-invite")
    public ResponseEntity<ApiResponse<String>> sendInviteCodesByEmail(
            @PathVariable Long calendarId,
            @AuthenticationPrincipal CustomUserDetails customUserDetails,
            @RequestBody InviteRequestDto inviteRequestDto) {

        String ownerEmail = customUserDetails.getUsername();
        log.info("POST /v1/calendars/{}/send-invite - {}가 {}명에게 초대 코드 전송 요청", calendarId, ownerEmail, inviteRequestDto.getEmailList().size());

        return ResponseEntity.ok(calendarService.sendInviteCodesByEmail(calendarId, ownerEmail, inviteRequestDto.getEmailList()));
    }
    @Operation(summary = "초대코드 입력 후 캘린더 가입", description = "이메일로 받은 초대코드를 입력하여 공용캘린더에 가입")
    @SecurityRequirement(name = "bearerAuth")
    @PostMapping("/v1/calendar/join")
    public ResponseEntity<ApiResponse<String>> joinCalendar(
            @AuthenticationPrincipal CustomUserDetails customUserDetails,
            @RequestBody CalendarJoinRequestDto calendarJoinRequestDto) {

        String email = customUserDetails.getUsername();
        String inviteCode = calendarJoinRequestDto.getInviteCode();

        if (inviteCode == null || inviteCode.trim().isEmpty() || inviteCode.length() != 8) {
            throw new AppException(ErrorCode.INVALID_INVITE_CODE, ErrorCode.INVALID_INVITE_CODE.getMessage());
        }

        log.info("초대 코드 입력 - 사용자: {}, 코드: {}", email, inviteCode);

        calendarService.joinCalendar(email, inviteCode);

        return ResponseEntity.ok(ApiResponse.success("공용 캘린더 가입이 완료되었습니다."));
    }

    @Operation(summary = "유저의 모든 캘린더 조회", description = "로그인한 유저가 사용 중인 캘린더 전체 조회")
    @SecurityRequirement(name = "bearerAuth")
    @GetMapping("/v1/calendars")
    public ResponseEntity<ApiResponse<List<CalendarResponseDto>>> getCalendarList(
            @AuthenticationPrincipal CustomUserDetails customUserDetails)
    {
        String email = customUserDetails.getUsername();

        List<CalendarResponseDto> calendarResponseDtoList = calendarService.getUserCalendars(email);

        return ResponseEntity.ok(ApiResponse.success(calendarResponseDtoList));
    }

    @Operation(summary = "캘린더에 속한 유저 조회", description = "공용 캘린더에 속한 모든 유저를 조회합니다.")
    @SecurityRequirement(name = "bearerAuth")
    @GetMapping("/v1/calendar/{calendarId}/member")
    public ResponseEntity<ApiResponse<List<CalendarMemberResponseDto>>> getCalendarMembers(
            @PathVariable Long calendarId,
            @AuthenticationPrincipal CustomUserDetails customUserDetails) {

        List<CalendarMemberResponseDto> members = calendarService.getCalendarMembers(calendarId, customUserDetails.getUsername());
        return ResponseEntity.ok(ApiResponse.success(members));
    }

    @Operation(summary = "캘린더의 멤버 삭제", description = "공용 캘린더에 속한 멤버를 선택하여 삭제")
    @SecurityRequirement(name = "bearerAuth")
    @DeleteMapping("/v1/calendar/{calendarId}")
    public ResponseEntity<ApiResponse<String>> removeMembers(
            @PathVariable Long calendarId,
            @RequestBody CalendarMemberDeleteRequestDto request,
            @AuthenticationPrincipal CustomUserDetails userDetails) {

        calendarService.removeMembersFromCalendar(calendarId, userDetails.getUsername(), request.getTargetEmails());
        return ResponseEntity.ok(ApiResponse.success("선택한 멤버들이 삭제되었습니다."));
    }
}
