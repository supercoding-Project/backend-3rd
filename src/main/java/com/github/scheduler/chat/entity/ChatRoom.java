package com.github.scheduler.chat.entity;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.github.scheduler.calendar.entity.CalendarEntity;
import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString
@Table(name = "chat_room")
@EntityListeners(AuditingEntityListener.class)
public class ChatRoom {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "name")
    private String name; //채팅방 이름

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "calendar_id" , nullable = false, unique = true)
    private CalendarEntity calendar;

    @CreatedDate
    @Column(name = "created_at", updatable = false, insertable = false)
    private LocalDateTime createdAt;
    
}
