package com.github.scheduler.todo.dto;

import com.github.scheduler.schedule.dto.RepeatScheduleDto;
import lombok.*;

import java.time.LocalDate;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TodoUpdateDto {
    private Long todoId; // 수정할 할 일을 식별하기 위한 ID
    private String todoContent;
    private LocalDate todoDate;
    private RepeatScheduleDto repeatSchedule;
    private String memo;
}