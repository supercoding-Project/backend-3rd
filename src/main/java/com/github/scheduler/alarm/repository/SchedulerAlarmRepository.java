package com.github.scheduler.alarm.repository;

import com.github.scheduler.alarm.entity.SchedulerAlarmEntity;
import com.github.scheduler.auth.entity.UserEntity;
import com.github.scheduler.calendar.entity.UserCalendarEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SchedulerAlarmRepository extends JpaRepository<SchedulerAlarmEntity, Long> {
    // 유저의 캘린더 목록 조회
    //List<UserCalendarEntity> findByUserEntity(UserEntity userEntity);
}
