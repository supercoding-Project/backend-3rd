package com.github.scheduler.admin.service;

import com.github.scheduler.admin.dto.user.AdminUserDetailResponseDTO;
import com.github.scheduler.admin.dto.user.AdminUserResponseDTO;
import com.github.scheduler.admin.dto.user.AdminUserUpdateDTO;
import com.github.scheduler.auth.entity.UserEntity;
import com.github.scheduler.auth.repository.UserRepository;
import com.github.scheduler.global.exception.AppException;
import com.github.scheduler.global.exception.ErrorCode;
import com.github.scheduler.global.util.PasswordUtil;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;


@Service
@RequiredArgsConstructor
@Slf4j
public class AdminUserService {

    private final UserRepository userRepository;
    private final PasswordUtil passwordUtil;

    public List<AdminUserResponseDTO> getAllUsers() {
        return userRepository.findAll().stream()
                .map(AdminUserResponseDTO::from)
                .collect(Collectors.toList());
    }

    public AdminUserDetailResponseDTO getUser(long id) {
        UserEntity user = userRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.ADMIN_USER_NOT_FOUND,ErrorCode.ADMIN_USER_NOT_FOUND.getMessage()));
        return AdminUserDetailResponseDTO.from(user);
    }

    public void updateUserStatus(long id, AdminUserUpdateDTO dto) {
        UserEntity user = userRepository.findById(id)
                 .orElseThrow(() -> new AppException(ErrorCode.ADMIN_USER_NOT_FOUND,ErrorCode.ADMIN_USER_NOT_FOUND.getMessage()));

        if (dto.getUsername() !=  null) {
            user.changeUsername(dto.getUsername());
        }

        if (dto.getEmail() !=  null) {
            user.changeEmail(dto.getEmail());
        }

        if (dto.getPhone() !=  null) {
            user.changePhone(dto.getPhone());
        }

        if (Boolean.TRUE.equals(dto.getResetPassword())) {
            String tempPassword = "reset1234!"; // 고정값
            log.info("✅[임시 비밀번호 발급] : " + tempPassword);
            user.changePassword(passwordUtil.encrypt(tempPassword));
        }

        userRepository.save(user);
    }

    @Transactional
    public void deleteUser(long id) {
        UserEntity user = userRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.ADMIN_USER_NOT_FOUND,ErrorCode.ADMIN_USER_NOT_FOUND.getMessage()));

        userRepository.delete(user);
    }
}
