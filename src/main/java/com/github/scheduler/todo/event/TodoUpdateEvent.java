package com.github.scheduler.todo.event;


public record TodoUpdateEvent(Long todoId, String message, boolean success) {
}
