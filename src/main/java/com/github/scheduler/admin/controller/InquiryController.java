package com.github.scheduler.admin.controller;

import com.github.scheduler.admin.dto.inquiry.*;
import com.github.scheduler.admin.service.InquiryService;
import com.github.scheduler.global.config.auth.custom.CustomUserDetails;
import com.github.scheduler.global.dto.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/inquiries")
public class InquiryController {

    private final InquiryService inquiryService;

    @Operation(summary = "전체 문의글 조회")  // 비공개 글은 제목만 or 비밀번호 확인 후 조회
    @GetMapping
    public ResponseEntity<ApiResponse<List<InquiryListResponseDTO>>> getAllInquiries() {
        List<InquiryListResponseDTO> inquiries = inquiryService.getAllInquiries();
        return ResponseEntity.ok(ApiResponse.success(inquiries));
    }

    @Operation(summary = "문의글 상세 조회")   // 비공개 글이면 403 or 비밀번호 확인 필요
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<InquiryDetailResponseDTO>> getInquiry(@PathVariable Long id) {
        InquiryDetailResponseDTO inquiry =  inquiryService.getInquiry(id);
        return ResponseEntity.ok(ApiResponse.success(inquiry));
    }

    @Operation(summary = "비밀번호 확인 후 문의글 조회")
    @PostMapping("/{id}/verify")
    @PreAuthorize("hasRole('ROLE_USER')")
    public ResponseEntity<ApiResponse<Boolean>> verifyPassword(@PathVariable Long id,
                                                               @RequestBody PasswordVerifyDTO dto) {
        Boolean verify =  inquiryService.verifyPassword(id,dto);
        return ResponseEntity.ok(ApiResponse.success(verify));
    }

    @Operation(summary = "문의글 작성")
    @PostMapping
    @PreAuthorize("hasRole('ROLE_USER')")
    public ResponseEntity<ApiResponse<String>> createInquiry(@RequestBody InquiryRequestDTO dto,
                                                             @AuthenticationPrincipal CustomUserDetails userDetails) {
        inquiryService.createInquiry(dto,userDetails.getUserEntity().getUserId());
        return ResponseEntity.ok(ApiResponse.success("✅문의글 등록이 완료되었습니다."));
    }

    @Operation(summary = "문의글 수정")
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ROLE_USER')")
    public ResponseEntity<ApiResponse<String>> modifyInquiry(@PathVariable Long id,
                                                             @RequestBody InquiryModifyRequestDTO dto,
                                                             @AuthenticationPrincipal CustomUserDetails userDetails) {
        inquiryService.updateInquiry(id,dto,userDetails.getUserEntity().getUserId());
        return ResponseEntity.ok(ApiResponse.success("✅문의글이 수정되었습니다."));
    }

    @Operation(summary = "문의글 삭제")
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ROLE_USER')")
    public ResponseEntity<ApiResponse<String>> deleteInquiry(@PathVariable Long id,
                                                             @AuthenticationPrincipal CustomUserDetails userDetails) {
        inquiryService.deleteInquiry(id,userDetails.getUserEntity().getUserId());
        return ResponseEntity.ok(ApiResponse.success("✅문의글이 삭제되었습니다."));
    }
}
