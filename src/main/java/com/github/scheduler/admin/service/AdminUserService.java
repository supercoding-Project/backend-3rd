package com.github.scheduler.admin.service;

import com.github.scheduler.admin.dto.user.AdminUserDetailResponseDTO;
import com.github.scheduler.admin.dto.user.AdminUserResponseDTO;
import com.github.scheduler.auth.entity.UserEntity;
import com.github.scheduler.auth.entity.UserStatus;
import com.github.scheduler.auth.repository.UserRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AdminUserService {

    private final UserRepository userRepository;

    public List<AdminUserResponseDTO> getAllUsers() {
        return userRepository.findByDeletedAtIsNull().stream()
                .map(AdminUserResponseDTO::from)
                .collect(Collectors.toList());
    }

    public AdminUserDetailResponseDTO getUser(long id) {
        UserEntity user = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("not found user"));
        return AdminUserDetailResponseDTO.from(user);
    }

    public void updateUserStatus(long id, UserStatus status) {
        UserEntity user = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("not found user"));

        user.setStatus(status);
        userRepository.save(user);
    }

    @Transactional
    public void deleteUser(long id) {
        UserEntity user = userRepository.findById(id)
                .orElseThrow(RuntimeException::new);

        user.delete();  // deletedAt 설정 (소프트삭제)
    }
}
