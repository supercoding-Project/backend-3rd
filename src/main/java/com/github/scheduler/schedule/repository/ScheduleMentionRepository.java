package com.github.scheduler.schedule.repository;

import com.github.scheduler.schedule.entity.ScheduleMentionEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ScheduleMentionRepository extends JpaRepository<ScheduleMentionEntity, Long> {
    void deleteBySchedule_ScheduleId(Long scheduleScheduleId);
}

