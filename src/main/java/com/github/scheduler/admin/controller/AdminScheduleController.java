package com.github.scheduler.admin.controller;

import com.github.scheduler.admin.dto.schedule.ResponseUserScheduleListDTO;
import com.github.scheduler.admin.dto.schedule.ScheduleModifyRequestDTO;
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

    @Operation(summary = "일정 수정 (공용 일정)")
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public ResponseEntity<ApiResponse<String>> modifySchedule(@PathVariable long id,
                                                              @RequestBody ScheduleModifyRequestDTO dto) {
        adminScheduleService.updateSchedule(id,dto);
        return ResponseEntity.ok(ApiResponse.success("✅일정이 수정되었습니다."));
    }


    @Operation(summary = "일정 삭제")
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public ResponseEntity<ApiResponse<String>> deleteSchedule(@PathVariable long id) {
        adminScheduleService.deleteSchedule(id);
        return ResponseEntity.ok(ApiResponse.success("✅일정이 삭제되었습니다."));
    }

}
