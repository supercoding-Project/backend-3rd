package com.github.scheduler.chat.repository;

import com.github.scheduler.chat.entity.ChatMessage;
import com.github.scheduler.chat.entity.ChatRoom;
import io.lettuce.core.dynamic.annotation.Param;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface ChatMessageRepository extends JpaRepository<ChatMessage, Long> {
    ChatMessage findFirstByChatRoomOrderByCreatedAtDesc(ChatRoom chatRoom);

    // 가장 최신 메시지 size 개수만큼 불러오기
    @Query("SELECT cm FROM ChatMessage cm WHERE cm.chatRoom = :chatRoom ORDER BY cm.createdAt ASC ")
    List<ChatMessage> findLatestMessagesByChatRoom(ChatRoom chatRoom);

    @Query("SELECT cm FROM ChatMessage cm WHERE cm.chatRoom = :chatRoom AND cm.id > :lastReadMessageId ORDER BY cm.createdAt ASC ")
    List<ChatMessage> findMessagesByChatRoomBefore(ChatRoom chatRoom, Long lastReadMessageId);

    @Query("""
        SELECT cm FROM ChatMessage cm WHERE cm.chatRoom = :chatRoom
            AND cm.id <= :lastReadMessageId 
            AND cm.createdAt >= :joinedAt ORDER BY cm.createdAt DESC
    """)
    Page<ChatMessage> findPreviousMessagesWithPagination(ChatRoom chatRoom, Long lastReadMessageId, LocalDateTime joinedAt, Pageable pageable);

    Integer countByChatRoomAndIdGreaterThan(ChatRoom chatRoom, Long idIsGreaterThan);
}
