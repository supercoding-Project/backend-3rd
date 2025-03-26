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
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;


@Service
@RequiredArgsConstructor
@Slf4j
public class AdminUserService {

    private final UserRepository userRepository;
    private final PasswordUtil passwordUtil;

    public Page<AdminUserResponseDTO> searchUsers(String keyword, LocalDate startDate, LocalDate endDate, Pageable pageable) {
        LocalDateTime startDateTime = (startDate != null) ? startDate.atStartOfDay() :null;
        LocalDateTime endDateTime = (endDate != null) ? endDate.atStartOfDay() :null;

        return userRepository.searchUsers(keyword,startDateTime,endDateTime,pageable)
                .map(AdminUserResponseDTO::from);
    }

    public AdminUserDetailResponseDTO getUser(long id) {
        UserEntity user = userRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.ADMIN_USER_NOT_FOUND,ErrorCode.ADMIN_USER_NOT_FOUND.getMessage()));
        return AdminUserDetailResponseDTO.from(user);
    }

    public void updateUser(long id, AdminUserUpdateDTO dto) {
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
            String tempPassword = UUID.randomUUID().toString().substring(0, 8);  // 8자리 랜덤 생성
            String encodedPassword = passwordUtil.encrypt(tempPassword);

            log.info("✅[임시 비밀번호 발급] : " + tempPassword);

            user.changePassword(encodedPassword);
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
