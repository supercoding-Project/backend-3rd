package com.github.scheduler.users.controller;

import com.github.scheduler.users.service.InquiryService;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/inquiries")
public class InquiryController {

    private final InquiryService inquiryService;

    @Operation(summary = "전체 문의글 조회 (사용자 전용) ")
    @GetMapping
    public void getAllInquiries() {}

    @Operation(summary = "문의글 작성")
    @PostMapping
    public void createInquiry() {}

    @Operation(summary = "문의글 상세 조회")
    @GetMapping("/{id}")
    public void getInquiry(@PathVariable Long id) {}

    @Operation(summary = "문의글 수정")
    @PutMapping("/{id}")
    public void modifyInquiry(@PathVariable Long id) {}

    @Operation(summary = "문의글 삭제")
    @DeleteMapping("/{id}")
    public void deleteInquiry(@PathVariable Long id) {}

    @Operation(summary = "비밀번호 확인 후 문의글 조회")
    @PostMapping("/{id}/verify")
    public void verifyPassword(@PathVariable Long id) {}


}
