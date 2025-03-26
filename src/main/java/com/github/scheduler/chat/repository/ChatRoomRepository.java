package com.github.scheduler.chat.repository;

import com.github.scheduler.calendar.entity.CalendarEntity;
import com.github.scheduler.chat.entity.ChatRoom;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ChatRoomRepository extends JpaRepository<ChatRoom, Long> {
    boolean existsByCalendar(CalendarEntity calendar);

    Optional<ChatRoom> findByCalendar(CalendarEntity calendar);
}
