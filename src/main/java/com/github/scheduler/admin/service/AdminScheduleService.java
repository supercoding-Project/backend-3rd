package com.github.scheduler.admin.service;

import com.github.scheduler.admin.dto.schedule.ResponseUserScheduleListDTO;
import com.github.scheduler.admin.dto.schedule.ScheduleModifyRequestDTO;
import com.github.scheduler.admin.dto.schedule.ScheduleSimpleDTO;
import com.github.scheduler.auth.entity.UserEntity;
import com.github.scheduler.auth.repository.UserRepository;
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
        List<UserEntity> users = userRepository.findByDeletedAtIsNull();

        return users.stream()
                .map(user -> {
                    List<ScheduleEntity> schedules = scheduleRepository.findByCreateUserIdAndScheduleStatusNot(user, ScheduleStatus.DELETED);
                    return ResponseUserScheduleListDTO.from(user, schedules);
                })
                .collect(Collectors.toList());
    }

    public List<ScheduleSimpleDTO> getUserSchedules(Long userId) {
        UserEntity user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("해당 유저를 찾을 수 없습니다."));

        List<ScheduleEntity> schedules = scheduleRepository.findByCreateUserIdAndScheduleStatusNot(user, ScheduleStatus.DELETED);
        return schedules.stream()
                .map(ScheduleSimpleDTO::from)
                .collect(Collectors.toList());
    }

    public void updateSchedule(long id, ScheduleModifyRequestDTO dto) {
        ScheduleEntity schedule = scheduleRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("해당 일정이 존재하지 않습니다."));

        if (schedule.isDeleted()) {
            throw new RuntimeException("삭제된 일정은 수정할 수 없습니다.");
        }
        // 공용 일정인지 확인
        if (schedule.getCalendar().getCalendarId() == null) {
            throw new RuntimeException("개인 일정은 관리자 권한으로 수정할 수 없습니다.");
        }

        schedule.updateScheduleInfo(
                dto.getTitle(),
                dto.getStartTime(),
                dto.getEndTime(),
                dto.getLocation(),
                dto.getMemo()
        );
    }

    public void deleteSchedule(long id) {
        ScheduleEntity schedule = scheduleRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("해당 일정이 존재하지 않습니다."));

        // 공용 일정인지 확인
        if (schedule.getCalendar().getCalendarId() == null) {
            throw new RuntimeException("개인 일정은 관리자 권한으로 삭제할 수 없습니다.");
        }

        if (schedule.isDeleted()) {
            throw new RuntimeException("이미 삭제된 일정입니다.");
        }
        schedule.softDelete();  // 내부 매서드로 소프트 딜리트 처리
    }
}
