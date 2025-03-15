package com.github.scheduler.schedule.repository;

import com.github.scheduler.schedule.entity.SchedulerEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;

public interface ScheduleRepository extends JpaRepository<SchedulerEntity, Long> {
    List<SchedulerEntity> findByStartTimeBetweenAndCreateUserId_UserId(LocalDateTime startTime, LocalDateTime startTime2, Long createUserId);

    List<SchedulerEntity> findByStartTimeBetweenAndCalendarIdIsNotNull(LocalDateTime startDateTime, LocalDateTime endDateTime);

    List<SchedulerEntity> findByStartTimeBetween(LocalDateTime startDateTime, LocalDateTime endDateTime);
}
