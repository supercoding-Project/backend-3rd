package com.github.scheduler.admin.controller;

import com.github.scheduler.admin.dto.inquiryAnswer.AnswerRequestDTO;
import com.github.scheduler.admin.service.InquiryAnswerService;
import com.github.scheduler.global.config.auth.custom.CustomUserDetails;
import com.github.scheduler.global.dto.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/admin/inquiry-answers")
public class InquiryAnswerController {

    private final InquiryAnswerService inquiryAnswerService;

    @Operation(summary = "문의글에 대한 답변 작성")
    @PostMapping("/{inquiryId}/answer")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public ResponseEntity<ApiResponse<String>> createAnswer(@PathVariable long inquiryId,
                                                            @RequestBody AnswerRequestDTO dto,
                                                            @AuthenticationPrincipal CustomUserDetails admin) {
        inquiryAnswerService.createAnswer(inquiryId,dto,admin.getUserEntity());
        return ResponseEntity.ok(ApiResponse.success("✅답변이 등록되었습니다."));
    }

    @Operation(summary = "문의글 답변 수정")
    @PutMapping("/{answerId}/answer")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public ResponseEntity<ApiResponse<String>> modifyAnswer(@PathVariable long answerId,
                                                            @RequestBody AnswerRequestDTO dto) {
        inquiryAnswerService.updateAnswer(answerId,dto);
        return ResponseEntity.ok(ApiResponse.success("✅답변이 수정되었습니다."));
    }

    @Operation(summary = "문의글 답변 삭제")
    @DeleteMapping("/{answerId}/answer")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public ResponseEntity<ApiResponse<String>> deleteAnswer(@PathVariable long answerId) {
        inquiryAnswerService.deleteAnswer(answerId);
        return ResponseEntity.ok(ApiResponse.success("✅답변이 삭제되었습니다."));
    }
}
