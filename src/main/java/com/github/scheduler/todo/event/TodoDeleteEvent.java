package com.github.scheduler.todo.event;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@AllArgsConstructor
@RequiredArgsConstructor
public class TodoDeleteEvent {
    private final Long todoId;
    private final String message;
    private final boolean success;
}
