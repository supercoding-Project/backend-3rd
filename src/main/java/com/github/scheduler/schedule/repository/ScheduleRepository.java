package com.github.scheduler.schedule.repository;

import com.github.scheduler.admin.repository.ScheduleRepositoryCustom;
import com.github.scheduler.auth.entity.UserEntity;
import com.github.scheduler.calendar.entity.CalendarEntity;
import com.github.scheduler.schedule.entity.ScheduleEntity;
import com.github.scheduler.schedule.entity.ScheduleStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface ScheduleRepository extends JpaRepository<ScheduleEntity, Long> , ScheduleRepositoryCustom {

    List<ScheduleEntity> findByCalendarCalendarIdAndStartTimeBetween(Long calendarCalendarId, LocalDateTime startTimeAfter, LocalDateTime startTimeBefore);

    List<ScheduleEntity> findByStartTimeBetweenAndCalendarCalendarIdIsNotNull(LocalDateTime startDateTime, LocalDateTime endDateTime);

    List<ScheduleEntity> findByStartTimeBetween(LocalDateTime startDateTime, LocalDateTime endDateTime);

    List<ScheduleEntity> findByCreateUserIdAndStartTimeLessThanEqualAndEndTimeGreaterThanEqual(
            UserEntity createUserId, LocalDateTime now1, LocalDateTime now2);

    List<ScheduleEntity> findAllByEndTimeBeforeAndScheduleStatus(LocalDateTime now, ScheduleStatus scheduleStatus);

    List<ScheduleEntity> findByCalendarCalendarIdInAndScheduleStatusNotAndStartTimeBetween(List<Long> calendarId, ScheduleStatus scheduleStatus, LocalDateTime startDateTime, LocalDateTime endDateTime);

    List<ScheduleEntity> findByCalendar(CalendarEntity calendar);

    List<ScheduleEntity> findByCreateUserIdAndScheduleStatusNot(UserEntity createUserId, ScheduleStatus scheduleStatus);
}
