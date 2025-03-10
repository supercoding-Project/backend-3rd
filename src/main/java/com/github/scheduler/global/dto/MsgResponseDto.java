package com.github.scheduler.global.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class MsgResponseDto {
    private String msg;
    private Integer statusCode;
}
