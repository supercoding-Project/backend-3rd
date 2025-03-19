package com.github.scheduler.chat.entity;

import java.time.LocalDateTime;

import com.github.scheduler.auth.entity.UserEntity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table(name = "chat_message")
public class ChatMessage {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "room_id")
    private ChatRoom chatRoom;

    @ManyToOne
    @JoinColumn(name = "user_id") 
    private UserEntity sendUser;
    
    @Column(name = "message")
    private String message;

    @Column(name = "file_url")
    private String fileURL;

    @Column(name = "created_at", updatable = false, insertable = false)
    private LocalDateTime createdAt;


}
