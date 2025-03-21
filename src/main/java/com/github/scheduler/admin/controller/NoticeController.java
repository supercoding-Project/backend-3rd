package com.github.scheduler.admin.controller;

import com.github.scheduler.admin.dto.notice.NoticeDetailResponseDTO;
import com.github.scheduler.admin.dto.notice.NoticeRequestDTO;
import com.github.scheduler.admin.dto.notice.NoticeResponseDTO;
import com.github.scheduler.admin.service.NoticeService;
import com.github.scheduler.global.dto.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/notices")
public class NoticeController {

    private final NoticeService noticeService;

    @Operation(summary = "전체 공지사항 조회")
    @GetMapping
    public ResponseEntity<ApiResponse<List<NoticeResponseDTO>>> getAllNotices() {
        List<NoticeResponseDTO> notices = noticeService.getAllNotices();
        return ResponseEntity.ok(ApiResponse.success(notices));
    }

    @Operation(summary = "공지사항 상세 조회")
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<NoticeDetailResponseDTO>> getNotice(@PathVariable long id) {
        NoticeDetailResponseDTO notice = noticeService.getNotice(id);
        return ResponseEntity.ok(ApiResponse.success(notice));
    }

    @Operation(summary = "공지사항 작성")
    @PostMapping
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public ResponseEntity<ApiResponse<String>> createNotice(@RequestBody NoticeRequestDTO dto) {
        noticeService.createNotice(dto);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("✅공지사항이 성공적으로 등록되었습니다."));
    }

    @Operation(summary = "공지사항 수정")
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public ResponseEntity<ApiResponse<String>> modifyNotice(@PathVariable long id) {
        noticeService.updateNotice(id);
        return ResponseEntity.ok(ApiResponse.success("✅공지사항이 수정되었습니다."));
    }

    @Operation(summary = "공지사항 삭제")
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public ResponseEntity<ApiResponse<String>> deleteNotice(@PathVariable long id) {
        noticeService.deleteNotice(id);
        return ResponseEntity.ok(ApiResponse.success("✅공지사항이 삭제되었습니다."));
    }

}
