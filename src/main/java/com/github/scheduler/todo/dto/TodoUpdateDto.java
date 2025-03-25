package com.github.scheduler.todo.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
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
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate todoDate;
    private Boolean completed;
    private RepeatScheduleDto repeatSchedule;
    private String memo;
}