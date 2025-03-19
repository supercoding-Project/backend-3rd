package com.github.scheduler.calendar.repository;

import com.github.scheduler.auth.entity.UserEntity;
import com.github.scheduler.calendar.entity.CalendarEntity;
import com.github.scheduler.calendar.entity.UserCalendarEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UserCalendarRepository extends JpaRepository<UserCalendarEntity, Long> {
    boolean existsByUserEntityAndCalendarEntity(UserEntity userEntity, CalendarEntity calendarEntity);
}
