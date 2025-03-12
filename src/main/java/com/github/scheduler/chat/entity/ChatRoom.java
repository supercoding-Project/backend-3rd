package com.github.scheduler.chat.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table(name = "chat_room")
public class ChatRoom {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "name")
    private String name; //채팅방 이름

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "calender_id" , nullable = false)
    private CalenderEntity calendar;

    @Column(name = "created_at", updatable = false, insertable = false)
    private LocalDateTime createdAt;
    
}
