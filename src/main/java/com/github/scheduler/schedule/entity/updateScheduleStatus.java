package com.github.scheduler.schedule.entity;

import com.github.scheduler.schedule.repository.ScheduleRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.OptimisticLockingFailureException;
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
        try {
            List<ScheduleEntity> list = scheduleRepository.findAllByEndTimeBeforeAndScheduleStatus(LocalDateTime.now(), ScheduleStatus.SCHEDULED);
            for (ScheduleEntity entity : list) {
                try {
                    entity.setScheduleStatus(ScheduleStatus.COMPLETED);
                    scheduleRepository.saveAndFlush(entity);
                    log.info("Schedule {} → COMPLETED", entity.getScheduleId());
                } catch (OptimisticLockingFailureException ex) {
                    log.warn("동시 수정 충돌 발생, ID={} 업데이트 스킵", entity.getScheduleId());
                }
            }
        } catch (Exception ex) {
            log.error("스케줄 상태 업데이트 전체 작업 중 예외 발생", ex);
        }
    }
}
