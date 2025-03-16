package com.github.scheduler.admin.controller;

import com.github.scheduler.admin.service.AdminUserService;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/admin/users")
public class AdminUserController {

    private final AdminUserService adminUserService;

    @Operation(summary = "전체 유저 목록 조회 (관리자 전용)")
    @GetMapping
    public void getAllUsers() {}

    @Operation(summary = "유저 상세 조회")
    @GetMapping("/{id}")
    public void getUser(@PathVariable long id) {}

    @Operation(summary = "유저 정보 수정")
    @PutMapping("/{id}")
    public void modifyUser(@PathVariable long id) {}


    @Operation(summary = "유저 삭제")
    @DeleteMapping("/{id}")
    public void deleteUser(@PathVariable long id) {}


}
