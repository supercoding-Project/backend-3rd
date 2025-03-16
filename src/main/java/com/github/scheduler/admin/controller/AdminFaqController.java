package com.github.scheduler.admin.controller;

import com.github.scheduler.admin.service.AdminFaqService;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/admin/faqs")
public class AdminFaqController {

    private final AdminFaqService faqService;

    @Operation(summary = "전체 FAQ 조회 (관리자 전용)")
    @GetMapping
    public void getAllFags() {}

    @Operation(summary = "FAQ 상세 조회")
    @GetMapping("/{id}")
    public void getFaq(@PathVariable long id) {}

    @Operation(summary = "FAQ 게시글 작성")
    @PostMapping
    public void createFaq() {}

    @Operation(summary = "FAQ 게시글 수정")
    @PutMapping("/{id}")
    public void modifyFaq(@PathVariable long id) {}

    @Operation(summary = "FAQ 게시글 삭제")
    @DeleteMapping("/{id}")
    public void deleteFaq(@PathVariable long id) {}

}
