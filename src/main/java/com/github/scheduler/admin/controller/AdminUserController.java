package com.github.scheduler.admin.controller;

import com.github.scheduler.admin.dto.user.AdminUserDetailResponseDTO;
import com.github.scheduler.admin.dto.user.AdminUserResponseDTO;
import com.github.scheduler.admin.dto.user.AdminUserUpdateDTO;
import com.github.scheduler.admin.service.AdminUserService;
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
import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/admin/users")
public class AdminUserController {

    private final AdminUserService adminUserService;

    @Operation(summary = "전체 유저 목록 조회 (검색 & 정렬 & 필터 & 페이징)")
    @GetMapping
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public ResponseEntity<ApiResponse<Page<AdminUserResponseDTO>>> getAllUsers(
            @RequestParam(required = false) String keyword,   // email,username 부분 검색
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            @PageableDefault(size = 10, sort = "createdAt" , direction = Sort.Direction.DESC) Pageable pageable
    ) {
        Page<AdminUserResponseDTO> users = adminUserService.searchUsers(keyword,startDate,endDate,pageable);
        return ResponseEntity.ok(ApiResponse.success(users));
    }

    @Operation(summary = "유저 상세 조회")
    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public ResponseEntity<ApiResponse<AdminUserDetailResponseDTO>> getUser(@PathVariable long id) {
        AdminUserDetailResponseDTO user = adminUserService.getUser(id);
        return ResponseEntity.ok(ApiResponse.success(user));

    }

    @Operation(summary = "유저 수정")
    @PatchMapping("/{id}/update")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public ResponseEntity<ApiResponse<String>> updateUser(@PathVariable long id,@RequestBody AdminUserUpdateDTO dto) {
            adminUserService.updateUserStatus(id,dto);
            return ResponseEntity.ok(ApiResponse.success("✅유저 정보가 수정되었습니다."));
    }

    @Operation(summary = "유저 삭제")
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public ResponseEntity<ApiResponse<String>> deleteUser(@PathVariable long id) {
        adminUserService.deleteUser(id);
        return ResponseEntity.ok(ApiResponse.success("✅ 유저가 탈퇴되었습니다."));

    }

}
