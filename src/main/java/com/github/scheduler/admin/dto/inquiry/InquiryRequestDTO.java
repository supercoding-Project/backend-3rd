package com.github.scheduler.admin.dto.inquiry;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.github.scheduler.admin.entity.InquiryCategory;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class InquiryRequestDTO {
    private String title;
    private String content;
    private String password;
    @JsonProperty("isPrivate")
    private boolean isPrivate;
    private InquiryCategory category;
}
