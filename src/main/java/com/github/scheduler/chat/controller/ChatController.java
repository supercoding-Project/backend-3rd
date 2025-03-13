package com.github.scheduler.chat.controller;

import com.github.scheduler.chat.entity.ChatRoom;
import com.github.scheduler.global.config.auth.custom.CustomUserDetails;
import com.github.scheduler.global.dto.ApiResponse;
import com.github.scheduler.global.exception.ErrorCode;
import io.swagger.v3.oas.annotations.Operation;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;


@Slf4j
@RestController
@RequestMapping("/api/chat")
@RequiredArgsConstructor
public class ChatController {
    // TODO : 채팅방 생성
    @Operation(summary = "채팅방 생성")
    @PostMapping("/rooms")
    public ResponseEntity<ApiResponse<ChatRoom>> createRoom(
            @AuthenticationPrincipal CustomUserDetails customUserDetails,
            @RequestBody String entity) {
        //TODO
        if (customUserDetails == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ApiResponse.fail(ErrorCode.NOT_FOUND_USER));
        }
        ChatRoom chatRoom = new ChatRoom();
        return ResponseEntity.ok(ApiResponse.success(chatRoom));
    }
    
    // TODO : 채팅방 입장
    @PostMapping("/rooms/{roomId}/users")
    public ResponseEntity<Void> joinRoom(@PathVariable Long roomId, @RequestBody String entity) {
        //TODO
        
        return ResponseEntity.ok().build();
    }
    
    // TODO : 메시지 전송
    //@MessageMapping("/{roomId}")
    //@SendTo("/topic/chat/{roomId}")
    //public ChatMessage sendMessage(@DesticationVariable Long roomId,)
    

}
