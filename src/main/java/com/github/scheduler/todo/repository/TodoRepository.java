package com.github.scheduler.todo.repository;

import com.github.scheduler.todo.entity.TodoEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface TodoRepository extends JpaRepository<TodoEntity, Long> {

    List<TodoEntity> findByCalendarCalendarIdInAndTodoDateBetween(List<Long> calendarId, LocalDate startDate, LocalDate endDate);
}
