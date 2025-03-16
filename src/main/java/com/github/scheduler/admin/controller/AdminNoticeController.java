package com.github.scheduler.admin.controller;

import com.github.scheduler.admin.service.AdminNoticeService;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/admin/notices")
public class AdminNoticeController {

    private final AdminNoticeService noticeService;

    @Operation(summary = "전체 공지사항 조회 (관리자 전용)")
    @GetMapping
    public void getAllNotices() {}

    @Operation(summary = "공지사항 상세 조회")
    @GetMapping("/{id}")
    public void getNotice(@PathVariable long id) {}

    @Operation(summary = "공지사항 작성")
    @PostMapping
    public void createNotice() {}

    @Operation(summary = "공지사항 수정")
    @PutMapping("/{id}")
    public void modifyNotice(@PathVariable long id) {}

    @Operation(summary = "공지사항 삭제")
    @DeleteMapping("/{id}")
    public void deleteNotice(@PathVariable long id) {}

}
