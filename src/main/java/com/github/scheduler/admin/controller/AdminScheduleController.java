package com.github.scheduler.admin.controller;

import com.github.scheduler.admin.dto.schedule.ResponseUserScheduleListDTO;
import com.github.scheduler.admin.dto.schedule.ScheduleSimpleDTO;
import com.github.scheduler.admin.service.AdminScheduleService;
import com.github.scheduler.global.dto.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/admin/schedules")
public class AdminScheduleController {

    private final AdminScheduleService adminScheduleService;

    @Operation(summary = "전체 유저 일정 조회")
    @GetMapping
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public ResponseEntity<ApiResponse<List<ResponseUserScheduleListDTO>>> getAllSchedules() {
        List<ResponseUserScheduleListDTO> result = adminScheduleService.getAllUserSchedule();
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
