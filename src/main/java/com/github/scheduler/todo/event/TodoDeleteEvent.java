package com.github.scheduler.todo.event;


public record TodoDeleteEvent(Long todoId, String message, boolean success) {
}
