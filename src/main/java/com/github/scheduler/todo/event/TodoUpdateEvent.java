package com.github.scheduler.todo.event;


import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@AllArgsConstructor
@RequiredArgsConstructor
public class TodoUpdateEvent{
    private Long todoId;
    private String message;
    private boolean success;
}
