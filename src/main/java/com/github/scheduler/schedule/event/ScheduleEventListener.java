package com.github.scheduler.schedule.event;

import com.github.scheduler.alarm.service.AlarmService;
import com.github.scheduler.auth.entity.UserEntity;
import com.github.scheduler.calendar.entity.UserCalendarEntity;
import com.github.scheduler.global.exception.AppException;
import com.github.scheduler.global.exception.ErrorCode;
import com.github.scheduler.schedule.entity.ScheduleEntity;
import com.github.scheduler.schedule.entity.ScheduleMentionEntity;
import com.github.scheduler.schedule.repository.ScheduleRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Slf4j
@Component
@RequiredArgsConstructor
public class ScheduleEventListener {


    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleScheduleUpdateSuccess(UpdateScheduleEvent event) {
            log.info("Schedule {} updated successfully. Message: {}", event.getScheduleId(), event.getMassage());
        if (event.isSuccess()) {
            log.info("Schedule {} updated successfully. Message: {}", event.scheduleId(), event.message());
        }
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_ROLLBACK)
    public void handleScheduleUpdateFail(UpdateScheduleEvent event) {
        if (!event.isSuccess()) {
            log.warn("Schedule {} update failed. Message: {}", event.getScheduleId(), event.getMassage());
        }
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleScheduleDeleteSuccess(DeleteScheduleEvent event) {
        if (event.isSuccess()) {
            log.info("Schedule {} deleted successfully. Message: {}", event.getScheduleId(), event.getMessage());
        if (event.isSuccess()) {
            log.info("Schedule {} deleted successfully. Message: {}", event.scheduleId(), event.message());
        }
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_ROLLBACK)
    public void handleScheduleDeleteFail(DeleteScheduleEvent event) {
        if (!event.isSuccess()) {
            log.warn("Schedule {} delete failed. Message: {}", event.getScheduleId(), event.getMessage());
        }
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    @Async("scheduleCreateExecutor")
    public void handleScheduleCreatedEvent(ScheduleCreatedEvent event) {
        log.info("이벤트 처리 시작 - scheduleId: {}, message: {}", event.getScheduleId(), event.getMessage());

    }
}
