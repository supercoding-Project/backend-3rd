package com.github.scheduler.admin.repository;

import com.github.scheduler.admin.dto.schedule.ResponseUserScheduleListDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.LocalDate;

public interface ScheduleRepositoryCustom {
    Page<ResponseUserScheduleListDTO> findAllUserSchedules(String keyword, LocalDate start, LocalDate end, Pageable pageable);
}
