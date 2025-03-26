package com.github.scheduler.admin.controller;

import com.github.scheduler.admin.dto.faq.FaqDetailResponseDTO;
import com.github.scheduler.admin.dto.faq.FaqListResponseDTO;
import com.github.scheduler.admin.dto.faq.FaqRequestDTO;
import com.github.scheduler.admin.entity.FaqCategory;
import com.github.scheduler.admin.service.FaqService;
import com.github.scheduler.global.dto.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/faqs")
public class FaqController {

    private final FaqService faqService;

    @Operation(summary = "전체 FAQ 조회 (검색 & 페이징)")
    @GetMapping
    public ResponseEntity<ApiResponse<Page<FaqListResponseDTO>>> getAllFaqs(
            @RequestParam(required = false) String keyword,
            @PageableDefault(size = 10,sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable
    ) {
        Page<FaqListResponseDTO> faqs = faqService.getAllFaqs(keyword,pageable);
        return ResponseEntity.ok(ApiResponse.success(faqs));
    }

    @Operation(summary = "FAQ 카테고리별 조회 (페이징)")
    @GetMapping(params = "category")
    public ResponseEntity<ApiResponse<Page<FaqListResponseDTO>>> getCategoryFaqs(
            @RequestParam(required = false) FaqCategory category,
            @PageableDefault(size = 10, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {
        Page<FaqListResponseDTO> faqs = faqService.getFaqsByCategory(category,pageable);
        return ResponseEntity.ok(ApiResponse.success(faqs));
    }

    @Operation(summary = "FAQ 상세 조회")
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<FaqDetailResponseDTO>> getFaq(@PathVariable long id) {
            FaqDetailResponseDTO faq = faqService.getFaq(id);
            return ResponseEntity.ok(ApiResponse.success(faq));
    }

    @Operation(summary = "FAQ 게시글 등록")
    @PostMapping
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public ResponseEntity<ApiResponse<String>> createFaq(@RequestBody FaqRequestDTO dto) {
        faqService.createFaq(dto);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("✅FAQ 게시글이 성공적으로 등록되었습니다."));
    }

    @Operation(summary = "FAQ 게시글 수정")
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public ResponseEntity<ApiResponse<String>> modifyFaq(@PathVariable long id, @RequestBody FaqRequestDTO dto) {
        faqService.updateFaq(id,dto);
        return ResponseEntity.ok(ApiResponse.success("✅FAQ 게시글이 수정되었습니다."));
    }

    @Operation(summary = "FAQ 게시글 삭제")
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public ResponseEntity<ApiResponse<String>> deleteFaq(@PathVariable long id) {
        faqService.deleteFaq(id);
        return ResponseEntity.ok(ApiResponse.success("✅FAQ 게시글이 삭제되었습니다."));
    }

}
