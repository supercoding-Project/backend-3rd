package com.github.scheduler.schedule.entity;

import com.github.scheduler.schedule.repository.ScheduleRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@RequiredArgsConstructor
@Component
public class updateScheduleStatus {

    private final ScheduleRepository scheduleRepository;

    @Scheduled(cron = "0 * * * * *") // 매 분 마다 실행
    public void updateCompletedSchedule() {
        List<ScheduleEntity> scheduleEntities = scheduleRepository.findAllByEndTimeBeforeAndScheduleStatus(
                LocalDateTime.now(), ScheduleStatus.SCHEDULED);

        for (ScheduleEntity scheduleEntity : scheduleEntities) {
            scheduleEntity.setScheduleStatus(ScheduleStatus.COMPLETED);
        }

        scheduleRepository.saveAll(scheduleEntities);
        log.info("Updated {} schedules to COMPLETED", scheduleEntities.size());
    }
}
