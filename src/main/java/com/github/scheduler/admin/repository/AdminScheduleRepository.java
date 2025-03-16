package com.github.scheduler.admin.repository;

import com.github.scheduler.schedule.entity.SchedulerEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AdminScheduleRepository extends JpaRepository<SchedulerEntity,Long> {
}
