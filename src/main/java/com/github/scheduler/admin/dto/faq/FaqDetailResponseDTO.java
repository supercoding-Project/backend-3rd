package com.github.scheduler.admin.dto.faq;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.github.scheduler.admin.entity.FaqCategory;
import com.github.scheduler.admin.entity.FaqEntity;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.LocalDate;

@Getter
@AllArgsConstructor
public class FaqDetailResponseDTO {
    private Long id;
    private String question;
    private String answer;
    private FaqCategory category;
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
    private LocalDate date;

    public static FaqDetailResponseDTO from(FaqEntity faq) {
        return new FaqDetailResponseDTO(
                faq.getId(),
                faq.getQuestion(),
                faq.getAnswer(),
                faq.getCategory(),
                faq.getCreatedAt().toLocalDate()

        );
    }
}
