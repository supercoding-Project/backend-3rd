package com.github.scheduler.schedule.repository;

import com.github.scheduler.schedule.entity.ScheduleEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface ScheduleRepository extends JpaRepository<ScheduleEntity, Long> {
    List<ScheduleEntity> findByCreateUserIdUserIdAndStartTimeBetween(Long userId, LocalDateTime start, LocalDateTime end);

    List<ScheduleEntity> findByCalendarIdIsNotNullAndStartTimeBetweenAndCreateUserId_UserId(LocalDateTime start, LocalDateTime end, Long userId);

}
