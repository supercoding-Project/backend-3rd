package com.github.scheduler.calendar.repository;

import com.github.scheduler.auth.entity.UserEntity;
import com.github.scheduler.calendar.entity.UserCalendarEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface UserCalendarRepository extends JpaRepository<UserCalendarEntity, Long> {
    // 유저의 캘린더 목록 조회
    List<UserCalendarEntity> findByUserEntity(UserEntity userEntity);
}
