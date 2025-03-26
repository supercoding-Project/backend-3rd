package com.github.scheduler.chat.service;

import com.github.scheduler.auth.entity.UserEntity;
import com.github.scheduler.auth.repository.UserRepository;
import com.github.scheduler.calendar.entity.CalendarEntity;
import com.github.scheduler.calendar.entity.UserCalendarEntity;
import com.github.scheduler.calendar.repository.CalendarRepository;
import com.github.scheduler.calendar.repository.UserCalendarRepository;
import com.github.scheduler.chat.dto.ChatRoomCreate;
import com.github.scheduler.chat.dto.ChatRoomDto;
import com.github.scheduler.chat.dto.ChatRoomUserDto;
import com.github.scheduler.chat.dto.LastReadMessage;
import com.github.scheduler.chat.entity.ChatRoom;
import com.github.scheduler.chat.entity.ChatRoomUser;
import com.github.scheduler.chat.event.ChatRoomCreateEvent;
import com.github.scheduler.chat.repository.ChatMessageRepository;
import com.github.scheduler.chat.repository.ChatRoomRepository;
import com.github.scheduler.chat.repository.ChatRoomUserRepository;
import com.github.scheduler.global.config.auth.custom.CustomUserDetails;
import com.github.scheduler.global.dto.ApiResponse;
import com.github.scheduler.global.exception.AppException;
import com.github.scheduler.global.exception.ErrorCode;
import com.github.scheduler.invite.service.InviteCodeService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class ChatRestService {
    private final InviteCodeService inviteCodeService;

    private final ChatRoomRepository chatRoomRepository;
    private final ChatRoomUserRepository chatRoomUserRepository;
    private final ChatMessageRepository chatMessageRepository;
    private final CalendarRepository calendarRepository;
    private final UserCalendarRepository userCalendarRepository;
    private final ApplicationEventPublisher eventPublisher;
    private final UserRepository userRepository;

    @Transactional
    public ResponseEntity<ApiResponse<ChatRoomDto>> createRoom( Long calendarId ,CustomUserDetails customUserDetails) {
        // 채팅방 생성
        // get calendar entity

        log.info("Received createRoom event:  calendarId={}, userId={}",calendarId, customUserDetails.getUserEntity().getUserId());
        CalendarEntity calendar = calendarRepository.findById(calendarId)
                .orElseThrow( () -> new AppException(ErrorCode.NOT_FOUND_CALENDAR,ErrorCode.NOT_FOUND_CALENDAR.getMessage()));
        // 해당 유저가 캘린더에 권한이 있는지 체크
        if (! userCalendarRepository.existsByUserEntityAndCalendarEntity(customUserDetails.getUserEntity(), calendar)) {
            throw new AppException(ErrorCode.UNAUTHORIZED_CALENDAR,ErrorCode.UNAUTHORIZED_CALENDAR.getMessage());
        }

        // 채팅방 중복 체크 : 한 캘린더 당 한개의 채팅방만 생성
        if (chatRoomRepository.existsByCalendar(calendar)){
            throw new AppException(ErrorCode.DUPLICATED_CHATROOM,ErrorCode.DUPLICATED_CHATROOM.getMessage());
        }
        // 채팅방 생성
        ChatRoom chatRoom = ChatRoom.builder()
                .name(calendar.getCalendarName())
                .calendar(calendar)
                .build();

        ChatRoom savedRoom = chatRoomRepository.save(chatRoom);
        // 만든 유저가 채팅방 입장
        ChatRoomUser joinUser = ChatRoomUser.builder()
                .chatRoom(savedRoom)
                .user(customUserDetails.getUserEntity())
                .lastReadMessageId(null)
                .build();
        chatRoomUserRepository.save(joinUser);

        ChatRoomDto chatRoomDto = ChatRoomDto.builder()
                .chatRoomId(savedRoom.getId())
                .roomName(savedRoom.getName())
                .calendarId(savedRoom.getCalendar().getCalendarId())
                .createdAt(savedRoom.getCreatedAt())
                .build();

        return ResponseEntity.ok(ApiResponse.success(chatRoomDto));

    }
    @Transactional
    public ResponseEntity<ApiResponse<List<ChatRoomUserDto>>> getChatRooms(CustomUserDetails customUserDetails) {
        // user 정보 조회
        UserEntity user = customUserDetails.getUserEntity();
        // user의 캘린더 조회
        List<ChatRoomUser> chatRoomUserList = chatRoomUserRepository.findByUser(user);
        if (chatRoomUserList.isEmpty()) {
            throw new AppException(ErrorCode.NOT_FOUND_CHATROOM,ErrorCode.NOT_FOUND_CHATROOM.getMessage());
        }
        List<ChatRoomUserDto> userChatRoomUserDtoList = chatRoomUserList.stream()
                .map(ChatRoomUserDto::toDto)
                .toList();
        return ResponseEntity.ok(ApiResponse.success(userChatRoomUserDtoList));

    }

    public ResponseEntity<ApiResponse<ChatRoomUserDto>> joinRoom(CustomUserDetails customUserDetails, String inviteCode) {
        Long calendarId = inviteCodeService.validateInviteCode(inviteCode);
        log.info("Redis에서 조회된 calendarId: {}", calendarId);
        UserEntity user = customUserDetails.getUserEntity();
        // 캘린더 검색
        CalendarEntity calendar = calendarRepository.findById(calendarId)
                .orElseThrow(() -> new AppException(ErrorCode.NOT_FOUND_CALENDAR,ErrorCode.NOT_FOUND_CALENDAR.getMessage()));

        // chat room 검색
        ChatRoom chatRoom = chatRoomRepository.findByCalendar(calendar)
                .orElseThrow(() -> new AppException(ErrorCode.NOT_FOUND_CHATROOM,ErrorCode.NOT_FOUND_CHATROOM.getMessage()));

        // last read message 가 null 일 경우 joined한 시기 이후 부터의 메시지 조회가 가능
        ChatRoomUser joinUser = chatRoomUserRepository.save(ChatRoomUser.builder()
                .chatRoom(chatRoom)
                .user(user)
                .lastReadMessageId(null)
                .build());

        return ResponseEntity.ok(ApiResponse.success(ChatRoomUserDto.toDto(joinUser)));
    }
//    public void updateLastReadMessage(Long roomId, LastReadMessage lastReadMessage, CustomUserDetails customUserDetails) {
//    }
//
//    public ResponseEntity<ApiResponse<List<ChatRoomDto>>> getChatRooms(CustomUserDetails customUserDetails) {
//        UserEntity user = customUserDetails.getUserEntity();
//        List<ChatRoomUser> chatRoomUserList = chatRoomUserRepository.findByUser(user);
//
//    }
}
