package com.github.scheduler.chat.service;

import com.corundumstudio.socketio.AckRequest;
import com.corundumstudio.socketio.SocketIOClient;
import com.github.scheduler.auth.entity.UserEntity;
import com.github.scheduler.auth.repository.UserRepository;
import com.github.scheduler.calendar.entity.CalendarEntity;
import com.github.scheduler.calendar.repository.CalendarRepository;
import com.github.scheduler.chat.dto.*;
import com.github.scheduler.chat.entity.ChatMessage;
import com.github.scheduler.chat.entity.ChatRoom;
import com.github.scheduler.chat.entity.ChatRoomUser;
import com.github.scheduler.chat.event.ChatMessageSendEvent;
import com.github.scheduler.chat.event.ChatRoomCreateEvent;
import com.github.scheduler.chat.event.ChatRoomJoinEvent;
import com.github.scheduler.chat.repository.ChatMessageRepository;
import com.github.scheduler.chat.repository.ChatRoomRepository;
import com.github.scheduler.chat.repository.ChatRoomUserRepository;
import com.github.scheduler.global.dto.ApiResponse;
import com.github.scheduler.global.exception.AppException;
import com.github.scheduler.global.exception.ErrorCode;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;

import lombok.AllArgsConstructor;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@AllArgsConstructor
public class ChatService {
    private final ChatRoomRepository chatRoomRepository;
    private final ChatRoomUserRepository chatRoomUserRepository;
    private final ChatMessageRepository chatMessageRepository;
    private final CalendarRepository calendarRepository;
    private final ApplicationEventPublisher eventPublisher;
    private final UserRepository userRepository;

    @Transactional
    public void createRoom(SocketIOClient client, ChatRoomCreate roomCreate, AckRequest ackSender) {
        // 채팅방 생성
        // get calendar entity
        log.info("Received createRoom event: name={}, calendarId={}, userId={}",
                roomCreate.getName(), roomCreate.getCalendarId(), roomCreate.getUserId());
        CalendarEntity calendar = calendarRepository.findById(roomCreate.getCalendarId())
                .orElseThrow( () -> new AppException(ErrorCode.NOT_FOUND_CALENDAR,ErrorCode.NOT_FOUND_CALENDAR.getMessage()));
        // 채팅방 중복 체크
        ChatRoom existRoom = chatRoomRepository.findByCalendar(calendar);
        if (existRoom != null) {
            throw new AppException(ErrorCode.DUPLICATED_CHATROOM,ErrorCode.DUPLICATED_CHATROOM.getMessage());
        }
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

        //return chatRoomDto;
    }
    public void onDisconnect(SocketIOClient client) {
        log.info("Client disconnected: {}", client.getSessionId());
    }
    @Transactional
    public void joinRoom(SocketIOClient client, ChatRoomJoinRequest request , AckRequest ackSender) {
        // 채팅방 입장
        log.info("Received joinRoom event: roomId={}, userId={}", request.getRoomId(), request.getUserId());
        // 채팅방 찾기
        ChatRoom chatRoom = findRoomById(request.getRoomId());
        // user 찾기
        UserEntity user = findUserById(request.getUserId());
        // 중복 찾기
        ChatRoomUser existUser = chatRoomUserRepository.findByChatRoomAndUser(chatRoom,user);
        if (existUser != null) {
            throw new AppException(ErrorCode.DUPLICATED_CHATROOM_USER,ErrorCode.DUPLICATED_CHATROOM_USER.getMessage());
        }
        ChatRoomUser chatRoomUser = ChatRoomUser.builder()
                .chatRoom(chatRoom)
                .user(user)
                .lastReadMessageId(null)
                .build();
        ChatRoomUser chatUser = chatRoomUserRepository.save(chatRoomUser);
        ChatRoomUserDto chatRoomUserDto = ChatRoomUserDto.builder()
                .chatRoom(chatUser.getChatRoom())
                .userId(chatUser.getUser().getUserId())
                .lastReadMessageId(chatUser.getLastReadMessageId())
                .joinedAt(chatUser.getJoinedAt())
                .build();

        eventPublisher.publishEvent(new ChatRoomJoinEvent(chatRoomUserDto,client));

    }

    @Transactional
    public void sendMessage(SocketIOClient client, ChatMessageRequest request, AckRequest ackSender) {
        //채팅방 찾기
        // 채팅방 찾기
        ChatRoom chatRoom = findRoomById(request.getRoomId());
        // user 찾기
        UserEntity sender = findUserById(request.getSendUserId());

        // 메시지 저장
        ChatMessage chatMessage = chatMessageRepository.save(
                ChatMessage.builder()
                        .chatRoom(chatRoom)
                        .sendUser(sender)
                        .message(request.getMessage())
                        .fileURL(request.getFileURL() != null ? request.getFileURL().orElse(null):null)
                        .build()
        );
        ChatMessageDto chatMessageDto = ChatMessageDto.builder()
                .messageId(chatMessage.getId())
                .roomName(chatRoom.getName())
                .chatRoomId(chatRoom.getId())
                .calendarId(chatRoom.getCalendar().getCalendarId())
                .senderId(chatMessage.getSendUser().getUserId())
                .message(chatMessage.getMessage())
                .createdAt(chatMessage.getCreatedAt())
                .build();

        eventPublisher.publishEvent(new ChatMessageSendEvent(chatMessageDto,client));

    }

    // TODO : 읽음 처리 (동시성 처리 필요)
    // sol.1 : pessimistic lock 데이터를 조회할 때 락을 걸어 순차적으로 처리
    // sol.2 : optimistic lock 충돌 가능성을 가정하고, 최종 커밋 시점에 변경 사항 확인 => 충돌 발생시 재시도 로직 추가
    // sol.3 : Redis 사용 - 동시성 보장+성능 최적화 => Redis 장애 시 데이터 유실 가능 이중화 고려해야함
    private ChatRoom findRoomById(Long roomId) {
        return chatRoomRepository.findById(roomId)
                .orElseThrow( () -> new AppException(ErrorCode.NOT_FOUND_CHATROOM,ErrorCode.NOT_FOUND_CHATROOM.getMessage()));
    }

    private UserEntity findUserById(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow( () -> new AppException(ErrorCode.NOT_FOUND_USER,ErrorCode.NOT_FOUND_USER.getMessage()));
    }
}
