package com.github.scheduler.todo.event;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Slf4j
@Component
public class TodoEventListener {

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleTodoUpdatedEvent(TodoUpdateEvent event) {
        log.info("Todo {} updated successfully. Message: {}", event.getTodoId(), event.getMessage());
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleTodoDeleteEvent(TodoDeleteEvent event) {
        log.info("Todo {} deleted successfully. Message: {}", event.getTodoId(), event.getMessage());
    }
}