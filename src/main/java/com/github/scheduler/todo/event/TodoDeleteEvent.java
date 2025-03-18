package com.github.scheduler.todo.event;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public class TodoDeleteEvent {
    private final Long todoId;
    private final String message;
}
