package com.github.scheduler.admin.dto.faq;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.github.scheduler.admin.entity.FaqEntity;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.LocalDate;

@Getter
@AllArgsConstructor
public class FaqListResponseDTO {
    private Long id;

    private String question;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
    private LocalDate creationDate;

    public static FaqListResponseDTO from(FaqEntity faq) {
        return new FaqListResponseDTO(
                faq.getId(),
                faq.getQuestion(),
                faq.getCreatedAt().toLocalDate()
        );
    }
}
