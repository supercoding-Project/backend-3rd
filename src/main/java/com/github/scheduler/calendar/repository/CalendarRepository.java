package com.github.scheduler.calendar.repository;

import com.github.scheduler.auth.entity.UserEntity;
import com.github.scheduler.calendar.entity.CalendarEntity;
import com.github.scheduler.calendar.entity.CalendarType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CalendarRepository extends JpaRepository<CalendarEntity, Long> {
    boolean existsByCalendarNameAndCalendarType(String calendarName, CalendarType calendarType);
    @Query("SELECT c FROM CalendarEntity c JOIN FETCH c.owner WHERE c.calendarId = :calendarId")
    CalendarEntity findByCalendarIdWithOwner(@Param("calendarId") Long calendarId);
}
