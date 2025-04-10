package com.github.scheduler.alarm.dto.mapper;

import com.github.scheduler.alarm.dto.ResponseAlarmDto;
import com.github.scheduler.alarm.dto.SchedulerAlarmDto;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Component
public class AlarmMapper {
    public ResponseAlarmDto toResponseDto(SchedulerAlarmDto alarm) {
        ResponseAlarmDto dto = new ResponseAlarmDto();
        dto.setId(alarm.getAlarmId());
        dto.setType(alarm.getType());
        dto.setCalendarName("공유 캘린더"); // 필요시 동적으로 설정
        dto.setEventName("이벤트 제목");   // 필요시 동적으로 설정
        dto.setLocation("장소 정보");       // 필요시 동적으로 설정
        dto.setMembers(3);                  // 필요시 동적으로 설정
        dto.setEventTime(LocalDateTime.now()); // 필요시 변경
        dto.setRead(alarm.isRead());
        return dto;
    }
}
