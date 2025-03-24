package com.github.scheduler.chat.controller;

import com.github.scheduler.chat.dto.LastReadMessage;
import com.github.scheduler.chat.entity.ChatMessage;
import com.github.scheduler.chat.service.ChatService;
import com.github.scheduler.global.config.auth.custom.CustomUserDetails;
import com.github.scheduler.global.dto.ApiResponse;
import com.github.scheduler.global.exception.ErrorCode;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.parameters.RequestBody;
import lombok.AllArgsConstructor;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequestMapping("/api/v1/chat")
@RequiredArgsConstructor
public class ChatRestController {
    private final ChatService chatService;

    @Operation(summary = "user가 마지막으로 읽은 메시지 id update")
    @PatchMapping("/rooms/{roomId}/last-read")
    public ResponseEntity<ApiResponse<Void>> updateLastReadMessage(
            @AuthenticationPrincipal CustomUserDetails customUserDetails,
            @PathVariable Long roomId,
            @RequestBody LastReadMessage lastReadMessage) {
        if (customUserDetails == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ApiResponse.fail(ErrorCode.UNAUTHORIZED_ACCESS));
        }
        // user가 마지막 메시지 아이디를 어떻게 가져오지?
        chatService.updateLastReadMessage(roomId,lastReadMessage,customUserDetails);
        return ResponseEntity.ok().build();
    }
}
