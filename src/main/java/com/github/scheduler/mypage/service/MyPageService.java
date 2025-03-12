package com.github.scheduler.mypage.service;

import com.github.scheduler.mypage.dto.UserDto;
import com.github.scheduler.auth.entity.UserEntity;
import com.github.scheduler.auth.repository.UserImageRepository;
import com.github.scheduler.auth.repository.UserRepository;
import com.github.scheduler.global.exception.AppException;
import com.github.scheduler.global.exception.ErrorCode;
import com.github.scheduler.global.dto.ApiResponse;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class MyPageService {

    private final UserRepository userRepository;
    private final UserImageRepository userImageRepository;

    //유저 정보 조회
    @Transactional
    public ApiResponse<UserDto> getMyPageUserDto(String email) {

        UserEntity userEntity = userRepository.findByEmail(email)
                .orElseThrow(() -> new AppException(ErrorCode.NOT_FOUND_USERINFO, ErrorCode.NOT_FOUND_USERINFO.getMessage()));

        if (userEntity.getDeletedAt() != null) {
            throw new AppException(ErrorCode.DELETE_USERINFO, ErrorCode.DELETE_USERINFO.getMessage());
        }
        String userImageUrl = getUserProfileImageUrl(userEntity);

        UserDto userDto = UserDto.builder()
                .email(userEntity.getEmail())
                .name(userEntity.getUsername())
                .userImageUrl(userImageUrl)
                .build();

        return ApiResponse.success(userDto);
    }

    private String getUserProfileImageUrl(UserEntity userEntity) {
        // TODO 이미지 url 별도로 가져오기
        return "";
    }
}
