package com.github.scheduler.alarm.repository;

import com.github.scheduler.alarm.entity.SchedulerAlarmEntity;
import com.github.scheduler.auth.entity.UserEntity;
import com.github.scheduler.calendar.entity.UserCalendarEntity;
import com.github.scheduler.schedule.entity.ScheduleEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface SchedulerAlarmRepository extends JpaRepository<SchedulerAlarmEntity, Long> {
    boolean existsByScheduleAndTypeAndCreatedAtAfter(ScheduleEntity schedule, String type, LocalDateTime createdAtAfter);
    // 유저의 캘린더 목록 조회
    //List<UserCalendarEntity> findByUserEntity(UserEntity userEntity);
}
