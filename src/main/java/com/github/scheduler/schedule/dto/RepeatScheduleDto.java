package com.github.scheduler.schedule.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

import java.time.LocalDate;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class RepeatScheduleDto {
    @Schema(example = "NONE(반복 없음), DAILY(매일 반복), WEEKLY(매주 반복), MONTHLY(매월 반복), YEARLY(매년 반복) 중 택 1")
    private String repeatType; //"NONE", "DAILY", "WEEKLY", "MONTHLY", "YEARLY"

    @Schema(example = "0 or 1")
    private Integer repeatInterval; // 반복 간격

    @Schema(example = "YYYY-MM-dd")
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate repeatEndDate; // 반복 종료 날짜
}
