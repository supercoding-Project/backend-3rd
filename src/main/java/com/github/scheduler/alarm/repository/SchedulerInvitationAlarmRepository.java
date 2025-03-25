package com.github.scheduler.alarm.repository;

import com.github.scheduler.alarm.entity.SchedulerInvitationAlarmEntity;
import io.lettuce.core.dynamic.annotation.Param;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SchedulerInvitationAlarmRepository extends JpaRepository<SchedulerInvitationAlarmEntity, Long> {

    // 특정 사용자와 캘린더 ID로 알림을 조회하는 예시
//    List<SchedulerInvitationAlarmEntity> findByUserIdAndCalendarId(Long userId, Long calendarId);
//
//    // 알림 읽음 여부를 업데이트하는 쿼리 메서드 예시
//    @Modifying
//    @Query("UPDATE SchedulerInvitationAlarmEntity s SET s.isChecked = true WHERE s.id = :alarmId")
//    void markAsChecked(@Param("alarmId") Long alarmId);
//
//    // 특정 캘린더에 대한 모든 알림을 조회하는 예시
//    List<SchedulerInvitationAlarmEntity> findByCalendarId(Long calendarId);
}