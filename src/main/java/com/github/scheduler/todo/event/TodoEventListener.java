package com.github.scheduler.todo.event;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Slf4j
@Component
public class TodoEventListener {

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleTodoUpdatedSuccess(TodoUpdateEvent event) {
        if (event.isSuccess()){
            log.info("Todo {} updated successfully. Message: {}", event.getTodoId(), event.getMessage());
        }
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_ROLLBACK)
    public void handleTodoUpdateFail(TodoUpdateEvent event){
        if (!event.isSuccess()){
            log.warn("Todo {} update failed. Message: {}", event.getTodoId(), event.getMessage());
        }
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleTodoDeleteSuccess(TodoDeleteEvent event) {
        if (event.isSuccess()){
            log.info("Todo {} delete successfully. Message: {}", event.getTodoId(), event.getMessage());
        }
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_ROLLBACK)
    public void handleTodoDeleteFail(TodoDeleteEvent event){
        if (!event.isSuccess()){
            log.warn("Todo {} delete failed. Message: {}", event.getTodoId(), event.getMessage());
        }
    }
}