package com.github.scheduler.schedule.event;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Slf4j
@Component
public class ScheduleEventListener {

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleTodoUpdatedEvent(UpdateScheduleEvent event) {
        log.info("Todo {} updated successfully. Message: {}", event.getScheduleId(), event.getMessage());
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleTodoDeleteEvent(DeleteScheduleEvent event) {
        log.info("Todo {} deleted successfully. Message: {}", event.getScheduleId(), event.getMessage());
    }
}
