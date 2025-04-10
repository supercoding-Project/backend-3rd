package com.github.scheduler.alarm.service;

import com.corundumstudio.socketio.SocketIOClient;
import com.corundumstudio.socketio.SocketIOServer;
import com.github.scheduler.alarm.dto.ResponseAlarmDto;
import com.github.scheduler.alarm.entity.SchedulerAlarmEntity;
import com.github.scheduler.alarm.repository.SchedulerAlarmRepository;
import com.github.scheduler.alarm.repository.SchedulerInvitationAlarmRepository;
import com.github.scheduler.auth.entity.UserEntity;
import com.github.scheduler.auth.repository.UserRepository;
import com.github.scheduler.calendar.entity.CalendarEntity;
import com.github.scheduler.schedule.entity.ScheduleEntity;
import com.github.scheduler.schedule.repository.ScheduleRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;
import static org.mockito.Mockito.*;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
public class AlarmServiceTest {

    @Mock
    private SchedulerAlarmRepository schedulerAlarmRepository;
    @Mock
    private SchedulerInvitationAlarmRepository schedulerInvitationAlarmRepository;
    @Mock
    private UserRepository userRepository;
    @Mock
    private ScheduleRepository scheduleRepository;
    @Mock
    private ApplicationEventPublisher eventPublisher;
    @Mock
    private SocketIOServer socketIOServer;

    @InjectMocks
    private AlarmService alarmService;

    @Mock
    private SocketIOClient mockClient;

    @Test
    public void testSendAlarmToUser() {
        // Given
        String email = "test1234@gmail.com";
        Long userId = 5L;
        UserEntity user = new UserEntity();
        user.setUserId(userId);
        when(userRepository.findByEmail(email)).thenReturn(Optional.of(user));
        when(socketIOServer.getAllClients()).thenReturn(Arrays.asList(mockClient));
        when(mockClient.get("userId")).thenReturn(userId);

        // 기본 데이터 생성 (필요한 데이터만 설정)
        ScheduleEntity schedule = new ScheduleEntity();
        schedule.setStartTime(java.time.LocalDateTime.now());
        SchedulerAlarmEntity alarm = SchedulerAlarmEntity.builder()
                .user(user)
                .calendar(new CalendarEntity(/* 필요한 데이터 */))
                .schedule(schedule)
                .type("event_started")
                .isChecked(false)
                .build();

        // When
        alarmService.sendAlarmToUser(email, alarm);

        // Then: mockClient의 sendEvent가 호출되었는지 확인
        verify(mockClient).sendEvent(eq("receiveAlarm"), any(ResponseAlarmDto.class));
    }
}