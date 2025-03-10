package com.github.scheduler.chat.controller;

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
    @PostMapping("/rooms")
    public ResponseEntity<?> createRoom(@RequestBody String entity) {
        //TODO
        
        return ResponseEntity.ok(entity);
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
