package com.github.scheduler.admin.dto.inquiry;

import com.github.scheduler.admin.dto.inquiryAnswer.InquiryAnswerDTO;
import com.github.scheduler.admin.entity.InquiryEntity;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class InquiryDetailResponseDTO {
    private Long id;
    private String title;
    private String content;
    private boolean isPrivate;
    private String category;
    private LocalDateTime createdAt;
    private boolean answered;

    private List<InquiryAnswerDTO> answers;

    public static InquiryDetailResponseDTO from(InquiryEntity entity) {
        return InquiryDetailResponseDTO.builder()
                .id(entity.getId())
                .title(entity.getTitle())
                .content(entity.getContent())
                .isPrivate(entity.isPrivate())
                .category(entity.getCategory().name())
                .createdAt(entity.getCreatedAt())
                .answered(entity.isAnswered())
                .answers(entity.getAnswers().stream()
                        .map(InquiryAnswerDTO::from)
                        .toList())
                .build();
    }
}
