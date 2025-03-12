package com.github.scheduler.schedule.service;

import com.github.scheduler.auth.entity.UserEntity;
import com.github.scheduler.auth.repository.UserRepository;
import com.github.scheduler.calender.entity.CalenderEntity;
import com.github.scheduler.global.config.auth.custom.CustomUserDetails;
import com.github.scheduler.global.exception.AppException;
import com.github.scheduler.global.exception.ErrorCode;
import com.github.scheduler.schedule.dto.*;
import com.github.scheduler.schedule.entity.RepeatType;
import com.github.scheduler.schedule.entity.ScheduleStatus;
import com.github.scheduler.schedule.entity.SchedulerEntity;
import com.github.scheduler.schedule.repository.ScheduleRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.*;
import java.time.format.DateTimeFormatter;
import java.time.temporal.TemporalAdjusters;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class ScheduleService {

    private final UserRepository userRepository;
    private final ScheduleRepository scheduleRepository;

    //일정 조회(monthly,weekly,daily)
    @Transactional
    public List<ScheduleDto> getSchedules(CustomUserDetails customUserDetails, String view, String date, String scheduleType) {
        if (customUserDetails == null) {
            throw new AppException(ErrorCode.NOT_FOUND_USER,ErrorCode.NOT_FOUND_USER.getMessage());
        }

    }


    //일정 등록
    @Transactional
    public List<CreateScheduleDto> createSchedule(CustomUserDetails customUserDetails, CreateScheduleDto createScheduleDto) {
        if (customUserDetails == null) {
            throw new AppException(ErrorCode.NOT_FOUND_USER,ErrorCode.NOT_FOUND_USER.getMessage());
        }
    }

    }

    //TODO:일정 수정
    @Transactional
    public List<UpdateScheduleDto> updateSchedule(CustomUserDetails customUserDetails, UpdateScheduleDto updateScheduleDto) {
        if (customUserDetails == null) {
            throw new AppException(ErrorCode.NOT_FOUND_USER,ErrorCode.NOT_FOUND_USER.getMessage());
        }
    }

    //TODO:일정 삭제
    @Transactional
    public DeleteScheduleDto deleteSchedule(CustomUserDetails customUserDetails , Long scheduleId){
        if (customUserDetails == null) {
            throw new AppException(ErrorCode.NOT_FOUND_USER,ErrorCode.NOT_FOUND_USER.getMessage());
        }
    }

}
