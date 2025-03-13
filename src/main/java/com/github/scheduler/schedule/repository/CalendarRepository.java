package com.github.scheduler.schedule.repository;

import com.github.scheduler.auth.entity.UserEntity;
import com.github.scheduler.calendar.entity.CalendarEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CalendarRepository extends JpaRepository<CalendarEntity, Long> {


    @Query(value = "SELECT c.* FROM calendar c " +
            "JOIN user_calendar uc ON c.calendar_id = uc.calendar_id " +
            "WHERE uc.user_id = :#{#userEntity.userId}", nativeQuery = true)

    List<CalendarEntity> findByUserId(@Param("userEntity") UserEntity userEntity);
}
