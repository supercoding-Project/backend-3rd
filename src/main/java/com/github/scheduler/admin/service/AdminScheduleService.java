package com.github.scheduler.admin.service;

import com.github.scheduler.admin.dto.schedule.ResponseUserScheduleListDTO;
import com.github.scheduler.admin.dto.schedule.ScheduleModifyRequestDTO;
import com.github.scheduler.admin.dto.schedule.ScheduleSimpleDTO;
import com.github.scheduler.auth.entity.UserEntity;
import com.github.scheduler.auth.repository.UserRepository;
import com.github.scheduler.global.exception.AppException;
import com.github.scheduler.global.exception.ErrorCode;
import com.github.scheduler.schedule.entity.ScheduleEntity;
import com.github.scheduler.schedule.entity.ScheduleStatus;
import com.github.scheduler.schedule.repository.ScheduleRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class AdminScheduleService {

    private final ScheduleRepository scheduleRepository;
    private final UserRepository userRepository;

    public List<ResponseUserScheduleListDTO> getAllUserSchedule() {
        List<UserEntity> users = userRepository.findAll();

        return users.stream()
                .map(user -> {
                    List<ScheduleEntity> schedules = scheduleRepository.findByCreateUserIdAndScheduleStatusNot(user, ScheduleStatus.DELETED);
                    return ResponseUserScheduleListDTO.from(user, schedules);
                })
                .collect(Collectors.toList());
    }

    public List<ScheduleSimpleDTO> getUserSchedules(Long userId) {
        UserEntity user = userRepository.findById(userId)
                .orElseThrow(() -> new AppException(ErrorCode.ADMIN_USER_NOT_FOUND,ErrorCode.ADMIN_USER_NOT_FOUND.getMessage()));

        List<ScheduleEntity> schedules = scheduleRepository.findByCreateUserIdAndScheduleStatusNot(user, ScheduleStatus.DELETED);
        return schedules.stream()
                .map(ScheduleSimpleDTO::from)
                .collect(Collectors.toList());
    }


}
