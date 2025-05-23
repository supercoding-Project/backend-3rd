package com.github.scheduler.chat.repository;

import com.github.scheduler.auth.entity.UserEntity;
import com.github.scheduler.chat.entity.ChatRoom;
import com.github.scheduler.chat.entity.ChatRoomUser;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ChatRoomUserRepository extends JpaRepository<ChatRoomUser, Long> {
    Optional<ChatRoomUser> findByChatRoomAndUser(ChatRoom chatRoom, UserEntity user);

    List<ChatRoomUser> findByUser(UserEntity user);
}
