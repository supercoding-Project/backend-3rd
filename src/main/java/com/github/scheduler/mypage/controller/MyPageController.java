package com.github.scheduler.mypage.controller;
import com.github.scheduler.mypage.dto.UserDto;
import com.github.scheduler.global.config.auth.custom.CustomUserDetails;
import com.github.scheduler.global.exception.ErrorCode;
import com.github.scheduler.global.dto.ApiResponse;
import com.github.scheduler.mypage.service.MyPageService;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;


@Slf4j
@RestController
@RequestMapping("/api/v1/mypage")
@RequiredArgsConstructor
public class MyPageController {

    private MyPageService myPageService;

    @Operation(summary = "유저 정보 조회", description = "")
    @GetMapping("/{email}")
    public ApiResponse<UserDto> getMyPageUserDto(
            @AuthenticationPrincipal CustomUserDetails customUserDetails,
            @PathVariable("email") String email)
    {
        if (!customUserDetails.getUsername().equals(email)) {
            return ApiResponse.fail(ErrorCode.NOT_FOUND_USERINFO);
        }
        return myPageService.getMyPageUserDto(email);
    }

    @Operation(summary = "유저 정보 수정", description = "")
    @PutMapping("/{email}/userInfo")
    public void updateMyPageUser() {
    }

    @Operation(summary = "프로필 이미지 수정", description = "")
    @PutMapping("/{email}/profileImage")
    public void updateUserProfileImage(){
    }

    @Operation(summary = "비밀번호 수정", description = "")
    @PutMapping("/{email}")
    public void updateUserPassword(){
    }
}
