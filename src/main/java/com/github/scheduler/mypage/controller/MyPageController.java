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

    private final MyPageService myPageService;

    @Operation(summary = "유저 정보 조회", description = "유저의 정보를 조회합니다.")
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

    @Operation(summary = "유저 정보 수정", description = "닉네임과 전화번호를 수정합니다.")
    @PutMapping
    public ResponseEntity<ApiResponse<UserDto>> updateMyPageUser(
            @AuthenticationPrincipal CustomUserDetails customUserDetails,
            @RequestBody UserDto userDto) {

        if (customUserDetails == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ApiResponse.fail(ErrorCode.UNAUTHORIZED_ACCESS));
        }

        String email = customUserDetails.getUsername();
        UserDto updatedUserDto = myPageService.updateUserInfo(email, userDto);
        return ResponseEntity.ok(ApiResponse.success(updatedUserDto));
    }

    @Operation(summary = "프로필 이미지 수정", description = "프로필 이미지를 별도로 수정합니다.")
    @PutMapping("/profileImage")
    public ResponseEntity<ApiResponse<UserDto>> updateUserProfileImage(
            @AuthenticationPrincipal CustomUserDetails customUserDetails,
            @RequestParam("file") MultipartFile file) {

        if (customUserDetails == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ApiResponse.fail(ErrorCode.UNAUTHORIZED_ACCESS));
        }

        String email = customUserDetails.getUsername();
        UserDto updatedUserDto = myPageService.updateUserProfileImage(email, file);
        return ResponseEntity.ok(ApiResponse.success(updatedUserDto)); // 수정된 UserDto 반환
    }

    @Operation(summary = "비밀번호 수정", description = "기존 비밀번호 확인 후 새 비밀번호로 수정합니다.")
    @PutMapping("/password")
    public ResponseEntity<ApiResponse<UserDto>> updateUserPassword(
            @AuthenticationPrincipal CustomUserDetails customUserDetails,
            @RequestParam("oldPassword") String oldPassword,
            @RequestParam("newPassword") String newPassword) {

        if (customUserDetails == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ApiResponse.fail(ErrorCode.UNAUTHORIZED_ACCESS));
        }

        String email = customUserDetails.getUsername();
        UserDto updatedUserDto = myPageService.updateUserPassword(email, oldPassword, newPassword);
        return ResponseEntity.ok(ApiResponse.success(updatedUserDto)); // 수정된 UserDto 반환
    }
}
