package com.github.scheduler.admin.controller;

import com.github.scheduler.users.service.InquiryService;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/admin/inquiries")
public class AdminInquiryController {

    private final InquiryService inquiryService;

    @Operation(summary = "문의글 전체 조회 (관리자 전용)")
    @GetMapping
    public void getAllInquiries() {}

    @Operation(summary = "문의글에 대한 답변 작성")
    @PostMapping("/{id}/answer")
    public void createAnswer(@PathVariable long id) {}

    @Operation(summary = "문의글 답변 수정")
    @PutMapping("/{id}/answer")
    public void modifyAnswer(@PathVariable long id) {}

    @Operation(summary = "문의글 답변 삭제")
    @DeleteMapping("/{id}/answer")
    public void deleteAnswer(@PathVariable long id) {}




}
