package com.github.scheduler.todo.event;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public class TodoUpdateEvent {
    private final Long todoId;
    private final String message;
    private final boolean success;
}
