package com.github.scheduler.chat.event;

import com.github.scheduler.global.exception.AppException;
import com.github.scheduler.global.exception.ErrorCode;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Slf4j
@Component
public class ChatRoomEventListener {

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleChatRoomCreated(ChatRoomCreateEvent event) {
        String roomId = event.getChatRoomDto().getChatRoomId()+"_"+event.getChatRoomDto().getRoomName();

        // room PK Key로 joinRoom 설정
        event.getClient().joinRoom(roomId);
        event.getClient().sendEvent("createRoom", event.getChatRoomDto());

        log.info("create room successful: {}", roomId);
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleChatRoomJoined(ChatRoomJoinEvent event) {
        String roomId = event.getChatRoomUserDto().getChatRoom().getId()+"_"
                +event.getChatRoomUserDto().getChatRoom().getName();

        try {
            event.getClient().joinRoom(roomId);
            event.getClient().sendEvent("joinRoom", "join success");//event.getChatRoomUserDto());
            log.info("Client joined successful: {}", roomId);
        } catch (Exception e) {
            throw new AppException(ErrorCode.INTERNAL_SERVER_ERROR, e.getMessage());
        }
    }

}
