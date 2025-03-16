package com.github.scheduler.users.controller;

import com.github.scheduler.users.service.NoticeService;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/notices")
public class NoticeController {

    private final NoticeService noticeService;

    @Operation(summary = "전체 공지사항 조회 (사용자 전용)")
    @GetMapping
    public void getAllNotices() {}

    @Operation(summary = "공지사항 상세 조회")
    @GetMapping("/{id}")
    public void getNotice(@PathVariable Long id) {}


}
