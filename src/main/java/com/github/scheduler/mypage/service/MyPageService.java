package com.github.scheduler.mypage.service;

import com.github.scheduler.auth.entity.UserImageEntity;
import com.github.scheduler.global.config.mypage.AwsFileService;
import com.github.scheduler.global.util.PasswordUtil;
import com.github.scheduler.mypage.dto.UserDto;
import com.github.scheduler.auth.entity.UserEntity;
import com.github.scheduler.auth.repository.UserImageRepository;
import com.github.scheduler.auth.repository.UserRepository;
import com.github.scheduler.global.exception.AppException;
import com.github.scheduler.global.exception.ErrorCode;
import com.github.scheduler.global.dto.ApiResponse;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.Objects;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class MyPageService {

    private final UserRepository userRepository;
    private final UserImageRepository userImageRepository;
    private final AwsFileService awsFileService;  // AwsFileService 추가
    private final PasswordUtil passwordUtil = new PasswordUtil();

    //유저 정보 조회
    @Transactional
    public UserDto getMyPageUserDto(String email) {

        UserEntity userEntity = userRepository.findByEmail(email)
                .orElseThrow(() -> new AppException(ErrorCode.NOT_FOUND_USERINFO, ErrorCode.NOT_FOUND_USERINFO.getMessage()));

        String userImageUrl = getUserProfileImageUrl(userEntity);

        return UserDto.builder()
                .email(userEntity.getEmail())
                .name(userEntity.getUsername())
                .phone(userEntity.getPhone())
                .userImageUrl(userImageUrl)
                .build();
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
    public UserDto updateUserInfo(String email, UserDto userDto) {
        UserEntity userEntity = userRepository.findByEmail(email)
                .orElseThrow(() -> new AppException(ErrorCode.NOT_FOUND_USERINFO, ErrorCode.NOT_FOUND_USERINFO.getMessage()));

        userEntity.setUsername(userDto.getName());
        userEntity.setPhone(userDto.getPhone());

        userRepository.save(userEntity);

        return new UserDto(userEntity.getUserId(), userEntity.getUsername(), userEntity.getEmail(),
                userEntity.getPhone(), userEntity.getUserImageEntity().getUrl());
    }


    // 프로필 이미지 수정
    @Transactional
    public UserDto updateUserProfileImage(String email, MultipartFile file) {

        UserEntity userEntity = userRepository.findByEmail(email)
                .orElseThrow(() -> new AppException(ErrorCode.NOT_FOUND_USERINFO, ErrorCode.NOT_FOUND_USERINFO.getMessage()));


        UserEntity managedUser = userRepository.findById(userEntity.getUserId())
                .orElseThrow(() -> new AppException(ErrorCode.NOT_FOUND_USERINFO, ErrorCode.NOT_FOUND_USERINFO.getMessage()));


        UserImageEntity existingImage = userImageRepository.findByUserEntity_Email(email);

        if (existingImage != null) {
            String newImageUrl = "";
            if(file.isEmpty()) {
                newImageUrl = "/uploads/profiles/base.png";
            }else{
                awsFileService.deletePhoto(existingImage.getUrl());
                newImageUrl = awsFileService.savePhoto(file, 1L);
            }
            existingImage.setUrl(newImageUrl);
            userImageRepository.save(existingImage);
        }

        return UserDto.builder()
                .id(managedUser.getUserId())
                .name(managedUser.getUsername())
                .email(managedUser.getEmail())
                .phone(managedUser.getPhone())
                .userImageUrl(existingImage != null ? existingImage.getUrl() : null)  // URL을 업데이트된 값으로 반환
                .build();
    }


    // 비밀번호 수정
    @Transactional
    public UserDto updateUserPassword(String email, String oldPassword, String newPassword) {
        UserEntity userEntity = userRepository.findByEmail(email)
                .orElseThrow(() -> new AppException(ErrorCode.NOT_FOUND_USERINFO, ErrorCode.NOT_FOUND_USERINFO.getMessage()));

        if (!Objects.equals(passwordUtil.encrypt(oldPassword), userEntity.getPassword())) {
            throw new AppException(ErrorCode.NOT_EQUAL_PASSWORD, ErrorCode.NOT_EQUAL_PASSWORD.getMessage());
        }

        userEntity.setPassword(passwordUtil.encrypt(newPassword));
        userRepository.save(userEntity);

        return UserDto.builder()
                .id(userEntity.getUserId())
                .name(userEntity.getUsername())
                .email(userEntity.getEmail())
                .phone(userEntity.getPhone())
                .userImageUrl(userEntity.getUserImageEntity().getUrl())
                .build();
    }


}
