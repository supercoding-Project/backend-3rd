package com.github.scheduler.chat.repository;

import com.github.scheduler.chat.entity.ChatMessage;
import com.github.scheduler.chat.entity.ChatRoom;
import io.lettuce.core.dynamic.annotation.Param;
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
    @Query(value = """
        (SELECT * FROM chat_message
            WHERE room_id = :chatRoomId
                AND id < :lastReadMessageId
            ORDER BY id DESC LIMIT 10)
        UNION ALL
        (SELECT * FROM chat_message
            WHERE room_id = :chatRoomId
            AND id >= :lastReadMessageId
            ORDER BY id ASC)   
        """,
        countQuery = "SELECT COUNT(*) FROM chat_message  WHERE roomId = :chatRoomId",
    nativeQuery = true)
    Page<ChatMessage> findMessagesByChatRoomBefore(@Param("ChatRoomId") Long chatRoomId,
                                                   @Param("lastReadMessageId") Long lastReadMessageId,
                                                   Pageable pageable);
}
