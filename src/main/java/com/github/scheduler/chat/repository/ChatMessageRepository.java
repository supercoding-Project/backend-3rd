package com.github.scheduler.chat.repository;

import com.github.scheduler.chat.entity.ChatMessage;
import com.github.scheduler.chat.entity.ChatRoom;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ChatMessageRepository extends JpaRepository<ChatMessage, Long> {
    ChatMessage findFirstByChatRoomOrderByCreatedAtDesc(ChatRoom chatRoom);

    List<ChatMessage> findByChatRoomAndIdGreaterThanEqualOrderByIdAsc(ChatRoom chatRoom, Long idIsGreaterThan, Pageable pageable);

    // 가장 최신 메시지 size 개수만큼 불러오기
    @Query("SELECT cm FROM ChatMessage cm WHERE cm.chatRoom = :chatRoom ORDER BY cm.createdAt ASC ")
    Page<ChatMessage> findLatestMessagesByChatRoom(ChatRoom chatRoom, Pageable pageable);

    // 채팅방의 메시지를 lastReadMessageId 기준으로 size 개수만큼 불러오기
    @Query("SELECT cm FROM ChatMessage cm WHERE cm.chatRoom = :chatRoom AND cm.id >= :lastReadMessageId ORDER BY cm.createdAt ASC ")
    Page<ChatMessage> findMessagesByChatRoomBefore(ChatRoom chatRoom, Long lastReadMessageId, Pageable pageable);
}
