package com.github.scheduler.admin.dto.faq;

import com.github.scheduler.admin.entity.FaqEntity;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.LocalDate;

@Getter
@AllArgsConstructor
public class FaqListResponseDTO {
    private Long id;
    private String question;
    private LocalDate creationDate;

    public static FaqListResponseDTO from(FaqEntity faq) {
        return new FaqListResponseDTO(
                faq.getId(),
                faq.getQuestion(),
                faq.getCreatedAt().toLocalDate()
        );
    }
}
