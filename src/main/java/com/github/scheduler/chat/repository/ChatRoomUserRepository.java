package com.github.scheduler.chat.repository;

import com.github.scheduler.auth.entity.UserEntity;
import com.github.scheduler.chat.entity.ChatRoom;
import com.github.scheduler.chat.entity.ChatRoomUser;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ChatRoomUserRepository extends JpaRepository<ChatRoomUser, Long> {
    ChatRoomUser findByChatRoomAndUser(ChatRoom chatRoom, UserEntity user);
}
