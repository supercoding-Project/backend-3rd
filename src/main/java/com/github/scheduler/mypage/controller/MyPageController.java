package com.github.scheduler.mypage.controller;
import com.github.scheduler.mypage.dto.UserDto;
import com.github.scheduler.global.config.auth.custom.CustomUserDetails;
import com.github.scheduler.global.exception.ErrorCode;
import com.github.scheduler.global.dto.ApiResponse;
import com.github.scheduler.mypage.service.MyPageService;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;


@Slf4j
@RestController
@RequestMapping("/api/v1/mypage")
@RequiredArgsConstructor
public class MyPageController {

    private MyPageService myPageService;

    @Operation(summary = "유저 정보 조회", description = "")
    @GetMapping
    public ResponseEntity<ApiResponse<UserDto>> getMyPageUserDto(
            @AuthenticationPrincipal CustomUserDetails customUserDetails)
    {
        if (customUserDetails == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ApiResponse.fail(ErrorCode.UNAUTHORIZED_ACCESS));
        }

        String email = customUserDetails.getUsername();
        return ResponseEntity.ok(ApiResponse.success(myPageService.getMyPageUserDto(email)));
    }

    @Operation(summary = "유저 정보 수정", description = "")
    @PutMapping
    public ResponseEntity<ApiResponse<Void>> updateMyPageUser(
            @AuthenticationPrincipal CustomUserDetails customUserDetails,
            @RequestBody UserDto userDto) {

        if (customUserDetails == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ApiResponse.fail(ErrorCode.UNAUTHORIZED_ACCESS));
        }

        String email = customUserDetails.getUsername();
        myPageService.updateUserInfo(email, userDto);
        return ResponseEntity.ok(ApiResponse.success(null));
    }

    @Operation(summary = "프로필 이미지 수정", description = "")
    @PutMapping("/profileImage")
    public ResponseEntity<ApiResponse<Void>> updateUserProfileImage(
            @AuthenticationPrincipal CustomUserDetails customUserDetails,
            @RequestParam("file") MultipartFile file) {

        if (customUserDetails == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ApiResponse.fail(ErrorCode.UNAUTHORIZED_ACCESS));
        }

        String email = customUserDetails.getUsername();
        myPageService.updateUserProfileImage(email, file);
        return ResponseEntity.ok(ApiResponse.success(null));
    }

    @Operation(summary = "비밀번호 수정", description = "")
    @PutMapping("/password")
    public ResponseEntity<ApiResponse<Void>> updateUserPassword(
            @AuthenticationPrincipal CustomUserDetails customUserDetails,
            @RequestParam("oldPassword") String oldPassword,
            @RequestParam("newPassword") String newPassword) {

        if (customUserDetails == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ApiResponse.fail(ErrorCode.UNAUTHORIZED_ACCESS));
        }

        String email = customUserDetails.getUsername();
        myPageService.updateUserPassword(email, oldPassword, newPassword);
        return ResponseEntity.ok(ApiResponse.success(null));
    }
}
