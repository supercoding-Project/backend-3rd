package com.github.scheduler.admin.dto.notice;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.github.scheduler.admin.entity.NoticeEntity;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.LocalDate;

@Getter
@AllArgsConstructor
public class NoticeDetailResponseDTO {
    private Long id;
    private String title;
    private String content;
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
    private LocalDate date;

    public static NoticeDetailResponseDTO from(NoticeEntity notice) {
        return new NoticeDetailResponseDTO(
                notice.getId(),
                notice.getTitle(),
                notice.getContent(),
                notice.getCreatedAt().toLocalDate()
        );
    }
}
