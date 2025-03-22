package com.github.scheduler.calendar.event;

import com.github.scheduler.auth.entity.UserEntity;
import com.github.scheduler.calendar.entity.CalendarEntity;
import com.github.scheduler.calendar.entity.UserCalendarEntity;
import com.github.scheduler.calendar.repository.UserCalendarRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Slf4j
@Component
@RequiredArgsConstructor
public class CalendarJoinedEventListener {

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleCalendarJoined(CalendarJoinedEvent event) {
        CalendarEntity calendar = event.getCalendar();
        UserEntity newUser = event.getNewUser();

        log.info("📢 공용 캘린더 참여 알림 - 캘린더 ID: {}, 사용자: {}", calendar.getCalendarId(), newUser.getUsername());
    }
}
