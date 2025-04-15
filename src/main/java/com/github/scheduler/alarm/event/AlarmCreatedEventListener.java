package com.github.scheduler.alarm.event;

import com.corundumstudio.socketio.SocketIOClient;
import com.github.scheduler.alarm.dto.ResponseAlarmDto;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class AlarmCreatedEventListener {

    @EventListener
    public void handleAlarmCreated(AlarmCreatedEvent event) {
        SocketIOClient client = event.getSocketIOClient();
        ResponseAlarmDto alarmDto = event.getAlarmData();

        if (client != null && alarmDto != null) {
            client.sendEvent("sendAlarm", alarmDto);
            log.info("🔔 클라이언트로 알림 전송 완료: {}", alarmDto);
        } else {
            log.warn("❌ 클라이언트나 알림이 null입니다.");
        }
    }
}
