package com.github.scheduler.admin.dto.notice;

import com.github.scheduler.admin.entity.NoticeEntity;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Getter
@AllArgsConstructor
public class NoticeResponseDTO {
    private Long id;
    private String title;
    private LocalDate date;

    public static NoticeResponseDTO from(NoticeEntity notice) {
        return new NoticeResponseDTO(
                notice.getId(),
                notice.getTitle(),
                notice.getCreatedAt().toLocalDate()
        );
    }
}
