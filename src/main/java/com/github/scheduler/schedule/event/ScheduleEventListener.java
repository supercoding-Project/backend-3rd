package com.github.scheduler.schedule.event;

import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Slf4j
@Component
public class ScheduleEventListener {

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleScheduleUpdateSuccess(UpdateScheduleEvent event) {
        if (event.isSuccess()) {
            log.info("Schedule {} updated successfully. Message: {}", event.getScheduleId(), event.getMessage());
        }
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_ROLLBACK)
    public void handleScheduleUpdateFail(UpdateScheduleEvent event) {
        if (!event.isSuccess()) {
            log.warn("Schedule {} update failed. Message: {}", event.getScheduleId(), event.getMessage());
        }
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleScheduleDeleteSuccess(DeleteScheduleEvent event) {
        if (event.isSuccess()) {
            log.info("Schedule {} deleted successfully. Message: {}", event.getScheduleId(), event.getMessage());
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
