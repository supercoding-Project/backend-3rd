package com.github.scheduler.calendar.repository;

import com.github.scheduler.auth.entity.UserEntity;
import com.github.scheduler.calendar.entity.CalendarEntity;
import com.github.scheduler.calendar.entity.UserCalendarEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface UserCalendarRepository extends JpaRepository<UserCalendarEntity, Long> {
    // 중복 가입 여부 확인 (JPQL 기반)
    @Query("SELECT COUNT(uc) > 0 FROM UserCalendarEntity uc WHERE uc.userEntity = :userEntity AND uc.calendarEntity = :calendarEntity")
    boolean existsByUserAndCalendar(@Param("userEntity") UserEntity userEntity, @Param("calendarEntity") CalendarEntity calendarEntity);

    // 특정 캘린더에 속한 모든 사용자 조회
    List<UserCalendarEntity> findByCalendarEntity(CalendarEntity calendarEntity);

    // 특정 사용자가 가입한 모든 캘린더 조회
    List<UserCalendarEntity> findByUserEntity(UserEntity userEntity);
}
