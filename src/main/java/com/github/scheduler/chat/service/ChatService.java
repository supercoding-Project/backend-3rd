package com.github.scheduler.chat.service;

import com.corundumstudio.socketio.SocketIOClient;
import com.github.scheduler.auth.service.UserService;
import com.github.scheduler.calendar.entity.CalendarEntity;
import com.github.scheduler.calendar.repository.CalendarRepository;
import com.github.scheduler.chat.dto.ChatRoomCreate;
import com.github.scheduler.chat.dto.ChatRoomDto;
import com.github.scheduler.chat.entity.ChatRoom;
import com.github.scheduler.chat.event.ChatRoomCreateEvent;
import com.github.scheduler.chat.repository.ChatMessageRepository;
import com.github.scheduler.chat.repository.ChatRoomRepository;
import com.github.scheduler.chat.repository.ChatRoomUserRepository;
import com.github.scheduler.global.dto.ApiResponse;
import com.github.scheduler.global.exception.AppException;
import com.github.scheduler.global.exception.ErrorCode;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;

import lombok.AllArgsConstructor;
import org.springframework.transaction.annotation.Transactional;

@Service
@AllArgsConstructor
public class ChatService {
    private final ChatRoomRepository chatRoomRepository;
    private final ChatRoomUserRepository chatRoomUserRepository;
    private final ChatMessageRepository chatMessageRepository;
    private final CalendarRepository calendarRepository;
    private final ApplicationEventPublisher eventPublisher;

    @Transactional
    public ApiResponse<ChatRoomDto> createRoom(ChatRoomCreate roomCreate, SocketIOClient client) {
        // 채팅방 생성
        // get calendar entity
        CalendarEntity calendar = calendarRepository.findById(roomCreate.getCalendarId())
                .orElseThrow( () -> new AppException(ErrorCode.NOT_FOUND_CALENDAR,ErrorCode.NOT_FOUND_CALENDAR.getMessage()));

        ChatRoom chatRoom = ChatRoom.builder()
                .name(roomCreate.getName())
                .calendar(calendar)
                .build();
        ChatRoom savedRoom = chatRoomRepository.save(chatRoom);
        ChatRoomDto chatRoomDto = ChatRoomDto.builder()
                .chatRoomId(savedRoom.getId())
                .roomName(savedRoom.getName())
                .calendarId(savedRoom.getCalendar().getCalendarId())
                .createdAt(savedRoom.getCreatedAt())
                .build();
        // 트랜잭션 후 실행할 event 발생
        eventPublisher.publishEvent(new ChatRoomCreateEvent(chatRoomDto,client));

        return ApiResponse.success(chatRoomDto);
    }

    // TODO : 읽음 처리 (동시성 처리 필요)
    // sol.1 : pessimistic lock 데이터를 조회할 때 락을 걸어 순차적으로 처리
    // sol.2 : optimistic lock 충돌 가능성을 가정하고, 최종 커밋 시점에 변경 사항 확인 => 충돌 발생시 재시도 로직 추가
    // sol.3 : Redis 사용 - 동시성 보장+성능 최적화 => Redis 장애 시 데이터 유실 가능 이중화 고려해야함
    
}
