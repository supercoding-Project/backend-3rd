package com.github.scheduler.alarm.event;

import com.corundumstudio.socketio.SocketIOClient;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Component
@RequiredArgsConstructor
public class AlarmSocketEventHandler {

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleAlarmCreatedEvent(AlarmCreatedEvent event) {
        SocketIOClient client = event.getSocketIOClient();
        client.sendEvent("alarm", event.getAlarmData());
    }
}
