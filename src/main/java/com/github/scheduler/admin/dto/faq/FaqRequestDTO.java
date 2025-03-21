package com.github.scheduler.admin.dto.faq;

import com.github.scheduler.admin.entity.FaqCategory;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class FaqRequestDTO {
    private String question;
    private String answer;
    private FaqCategory category;
}
