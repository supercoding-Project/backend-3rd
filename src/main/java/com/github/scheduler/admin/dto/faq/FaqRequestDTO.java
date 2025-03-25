package com.github.scheduler.admin.dto.faq;

import com.github.scheduler.admin.entity.FaqCategory;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@AllArgsConstructor
@NoArgsConstructor
public class FaqRequestDTO {
    private String question;
    private String answer;
    private FaqCategory category;
}
