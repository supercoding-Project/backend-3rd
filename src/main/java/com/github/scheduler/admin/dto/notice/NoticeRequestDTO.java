package com.github.scheduler.admin.dto.notice;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class NoticeRequestDTO {
    private String title;
    private String content;
}
