package com.github.scheduler.users.controller;

import com.github.scheduler.users.service.FaqService;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/Faqs")
public class FaqController {

    private final FaqService faqService;

    @Operation(summary = "전체 FAQ 조회 (사용자 전용)")
    @GetMapping
    public void getAllFaqs() {}

    @Operation(summary = "FAQ 상세 조회")
    @GetMapping("/{id}")
    public void getFaq(@PathVariable Long id) {}
}
