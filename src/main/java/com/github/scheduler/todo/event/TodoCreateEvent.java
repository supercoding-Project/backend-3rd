package com.github.scheduler.todo.event;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@AllArgsConstructor
@RequiredArgsConstructor
public class TodoCreateEvent {
    private Long todoId;
    private String message;
}
