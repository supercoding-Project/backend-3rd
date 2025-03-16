package com.github.scheduler.admin.controller;

import com.github.scheduler.admin.service.AdminScheduleService;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/admin/schedules")
public class AdminScheduleController {

    private final AdminScheduleService adminScheduleService;

    @Operation(summary = "전체 유저 일정 조회 (관리자 전용)")
    @GetMapping
    public void getAllSchedules() {}

    @Operation(summary = "일정 수정")
    @PutMapping("/{id}")
    public void modifySchedule(@PathVariable long id) {}


    @Operation(summary = "일정 삭제")
    @DeleteMapping("/{id}")
    public void deleteSchedule(@PathVariable long id) {}


}
