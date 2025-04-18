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

    List<SchedulerInvitationAlarmEntity> findByUser_UserIdAndIsCheckedFalse(Long userId);

    List<SchedulerInvitationAlarmEntity> findUnreadAlarmsByUser_UserId(Long userId);
}