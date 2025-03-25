package com.github.scheduler.chat.event;

import com.github.scheduler.chat.entity.ChatRoom;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Slf4j
@Component
public class ChatMessageEventListener {
    @Async
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleChatMessageSend(ChatMessageSendEvent event){
        //roomID
        String roomId = event.getChatMessageDto().getChatRoomId()+"_"+event.getChatMessageDto().getRoomName();

        event.getClient().getNamespace()
                .getRoomOperations(roomId)
                .sendEvent("receiveMessage",event.getChatMessageDto());
        log.info(event.getChatMessageDto().toString());
        log.info("Message sent to room {}: {}", roomId, event.getChatMessageDto().getMessage());
    }
}
