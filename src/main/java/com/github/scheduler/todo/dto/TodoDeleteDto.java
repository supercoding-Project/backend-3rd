package com.github.scheduler.todo.dto;

import lombok.*;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TodoDeleteDto {
    private Long todoId;
    private String message;
}
