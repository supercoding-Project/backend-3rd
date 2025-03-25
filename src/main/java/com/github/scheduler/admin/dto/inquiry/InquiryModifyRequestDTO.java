package com.github.scheduler.admin.dto.inquiry;

import com.github.scheduler.admin.entity.InquiryCategory;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class InquiryModifyRequestDTO {
    private String title;
    private String content;
    private String password;
    private boolean isPrivate;
    private InquiryCategory category;
}
