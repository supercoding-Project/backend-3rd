package com.github.scheduler.mypage.service;

import com.github.scheduler.auth.entity.UserImageEntity;
import com.github.scheduler.global.config.mypage.AwsFileService;
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
import org.springframework.web.multipart.MultipartFile;

import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class MyPageService {

    private final UserRepository userRepository;
    private final UserImageRepository userImageRepository;
    private final AwsFileService awsFileService;  // AwsFileService 추가

    //유저 정보 조회
    @Transactional
    public UserDto getMyPageUserDto(String email) {

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

        return userDto;
    }

    // 유저 프로필 이미지 가져오기
    private String getUserProfileImageUrl(UserEntity userEntity) {
        UserImageEntity userImageEntity = userImageRepository.findByUserEntity_Email(userEntity.getEmail());

        if (userImageEntity != null) {
            return userImageEntity.getUrl();  // S3 이미지 반환
        } else {
            // 기본 이미지 반환
            return "/uploads/profiles/base.png";
        }
    }

    // 유저 정보 수정
    @Transactional
    public void updateUserInfo(String email, UserDto userDto) {
        UserEntity userEntity = userRepository.findByEmail(email)
                .orElseThrow(() -> new AppException(ErrorCode.NOT_FOUND_USERINFO, ErrorCode.NOT_FOUND_USERINFO.getMessage()));

        userEntity.setUsername(userDto.getName());

        userRepository.save(userEntity);
    }

    // 프로필 이미지 수정
    @Transactional
    public UserDto updateUserProfileImage(String email, MultipartFile file) {
        UserEntity userEntity = userRepository.findByEmail(email)
                .orElseThrow(() -> new AppException(ErrorCode.NOT_FOUND_USERINFO, ErrorCode.NOT_FOUND_USERINFO.getMessage()));


        UserImageEntity existingImage = userImageRepository.findByUserEntity_Email(email);
        if (existingImage != null) {
            awsFileService.deletePhoto(existingImage.getUrl());  // 기존 이미지 삭제
        }

        String imageUrl = awsFileService.savePhoto(file, userEntity.getUserId());

        UserImageEntity userImageEntity = new UserImageEntity();
        userImageEntity.setUserEntity(userEntity);
        userImageEntity.setUrl(imageUrl);
        userImageRepository.save(userImageEntity);

        return UserDto.builder()
                .id(userEntity.getUserId())
                .name(userEntity.getUsername())
                .email(userEntity.getEmail())
                .userImageUrl(imageUrl)
                .build();
    }

    // 비밀번호 수정
    @Transactional
    public void updateUserPassword(String email, String oldPassword, String newPassword) {
        UserEntity userEntity = userRepository.findByEmail(email)
                .orElseThrow(() -> new AppException(ErrorCode.NOT_FOUND_USERINFO, ErrorCode.NOT_FOUND_USERINFO.getMessage()));

        if (!userEntity.getPassword().equals(oldPassword)) {
            throw new AppException(ErrorCode.INVALID_PASSWORD, ErrorCode.INVALID_PASSWORD.getMessage());
        }

        userEntity.setPassword(newPassword);  // 비밀번호 수정
        userRepository.save(userEntity);
    }

}
