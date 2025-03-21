package com.github.scheduler.admin.dto.inquiry;

import com.github.scheduler.admin.entity.InquiryEntity;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class InquiryListResponseDTO {
    private Long id;
    private String title;
    private boolean answer;     // 답변여부
    private boolean isPrivate;  // 공개여부
    private String category;
    private LocalDate createdAt;

    public static InquiryListResponseDTO of(InquiryEntity inquiry) {
        return InquiryListResponseDTO.builder()
                .id(inquiry.getId())
                .title(inquiry.getTitle())
                .answer(inquiry.isAnswered())
                .isPrivate(inquiry.isPrivate())
                .category(inquiry.getCategory().name())
                .createdAt(inquiry.getCreatedAt().toLocalDate())
                .build();
    }

    // 비공개 글은 제목만 보여주고 내용 숨기기
    public static InquiryListResponseDTO ofHidden(InquiryEntity inquiry) {
        return InquiryListResponseDTO.builder()
                .id(inquiry.getId())
                .title("[비공개 글입니다]")
                .answer(inquiry.isAnswered())
                .isPrivate(true) // 비공개
                .category(inquiry.getCategory().name())
                .createdAt(inquiry.getCreatedAt().toLocalDate())
                .build();
    }

}
