package com.github.scheduler.admin.controller;

import com.github.scheduler.admin.dto.schedule.ResponseUserScheduleListDTO;
import com.github.scheduler.admin.dto.schedule.ScheduleSimpleDTO;
import com.github.scheduler.admin.service.AdminScheduleService;
import com.github.scheduler.global.dto.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/admin/schedules")
public class AdminScheduleController {

    private final AdminScheduleService adminScheduleService;

    @Operation(summary = "전체 유저 일정 조회 (검색 & 필터링 & 정렬 & 페이징)")
    @GetMapping
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public ResponseEntity<ApiResponse<Page<ResponseUserScheduleListDTO>>> getAllSchedules(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate start,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate end,
            @PageableDefault(size = 10, sort = "startTime" , direction = Sort.Direction.DESC) Pageable pageable
    ) {
        Page<ResponseUserScheduleListDTO> result = adminScheduleService.getAllUserSchedule(keyword,start,end,pageable);
        return ResponseEntity.ok(ApiResponse.success(result));
    }

    @Operation(summary = "특정 유저 전체 일정 조회")
    @GetMapping("/users/{userId}")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public ResponseEntity<ApiResponse<List<ScheduleSimpleDTO>>> getUserSchedules(@PathVariable Long userId) {
        List<ScheduleSimpleDTO> schedules =  adminScheduleService.getUserSchedules(userId);
        return ResponseEntity.ok(ApiResponse.success(schedules));
    }

}
