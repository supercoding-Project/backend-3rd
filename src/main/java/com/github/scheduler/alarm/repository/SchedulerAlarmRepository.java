package com.github.scheduler.alarm.repository;

import com.github.scheduler.auth.entity.UserEntity;
import com.github.scheduler.calender.entity.UserCalenderEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SchedulerAlarmRepository extends JpaRepository<UserCalenderEntity, Long> {
    // 유저의 캘린더 목록 조회
    List<UserCalenderEntity> findByUserEntity(UserEntity userEntity);
}
