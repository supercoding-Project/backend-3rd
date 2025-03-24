package com.github.scheduler.calendar.repository;

import com.github.scheduler.auth.entity.UserEntity;
import com.github.scheduler.calendar.entity.CalendarEntity;
import com.github.scheduler.calendar.entity.UserCalendarEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserCalendarRepository extends JpaRepository<UserCalendarEntity, Long> {
    boolean existsByUserEntityAndCalendarEntity(UserEntity userEntity, CalendarEntity calendarEntity);

    // 특정 캘린더에 속한 모든 사용자 조회
    List<UserCalendarEntity> findByCalendarEntity(CalendarEntity calendarEntity);

    // 특정 사용자가 가입한 모든 캘린더 조회
    List<UserCalendarEntity> findByUserEntity(UserEntity userEntity);

    List<UserCalendarEntity> findByCalendarEntityCalendarId(Long calendarId);

    boolean existsByCalendarEntityCalendarIdAndUserEntityUserId(Long calendarId, Long currentUserId);

    void deleteByUserEntity(UserEntity userEntity);

    void deleteByUserEntityAndCalendarEntity(UserEntity userEntity, CalendarEntity calendar);

    void deleteByCalendarEntity(CalendarEntity calendar);

    Optional<UserCalendarEntity> findByUserEntityAndCalendarEntity(UserEntity targetUser, CalendarEntity calendar);
}
