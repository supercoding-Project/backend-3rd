package com.github.scheduler.admin.dto.inquiryAnswer;

import com.github.scheduler.admin.entity.InquiryAnswerEntity;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class InquiryAnswerDTO {
    private Long id;
    private String content;
    private String adminName;
    private LocalDateTime answeredAt;

    public static InquiryAnswerDTO from(InquiryAnswerEntity answer) {
        return InquiryAnswerDTO.builder()
                .id(answer.getId())
                .content(answer.getContent())
                .adminName(answer.getAdmin().getUsername()) // 관리자 이름
                .answeredAt(answer.getAnsweredAt())
                .build();
    }
}
