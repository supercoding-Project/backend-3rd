package com.github.scheduler.chat.service;

import org.springframework.stereotype.Service;

import lombok.AllArgsConstructor;

@Service
@AllArgsConstructor
public class ChatService {

    // TODO : 읽음 처리 (동시성 처리 필요)
    // sol.1 : pessimistic lock 데이터를 조회할 때 락을 걸어 순차적으로 처리
    // sol.2 : optimistic lock 충돌 가능성을 가정하고, 최종 커밋 시점에 변경 사항 확인 => 충돌 발생시 재시도 로직 추가
    // sol.3 : Redis 사용 - 동시성 보장+성능 최적화 => Redis 장애 시 데이터 유실 가능 이중화 고려해야함
    
}
