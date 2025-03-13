package com.github.scheduler.schedule.repository;

import com.github.scheduler.schedule.entity.SchedulerEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface ScheduleRepository extends JpaRepository<SchedulerEntity, Long> {
    List<SchedulerEntity> findByStartTimeBetweenAndCreateUserId_UserId(LocalDateTime startTime, LocalDateTime startTime2, Long createUserId);

    List<SchedulerEntity> findByStartTimeBetweenAndCalendarIdIsNotNull(LocalDateTime startDateTime, LocalDateTime endDateTime);

    List<SchedulerEntity> findByStartTimeBetween(LocalDateTime startDateTime, LocalDateTime endDateTime);

    List<SchedulerEntity> findByCalendarIdInAndStartTimeBetween(List<Long> calendarIds, LocalDateTime startDateTime, LocalDateTime endDateTime);
}
