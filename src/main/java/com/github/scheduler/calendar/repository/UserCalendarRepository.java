package com.github.scheduler.calendar.repository;

import com.github.scheduler.auth.entity.UserEntity;
import com.github.scheduler.calendar.entity.UserCalendarEntity;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface UserCalendarRepository extends JpaRepository<UserCalendarEntity, Long> {
    @EntityGraph(attributePaths = {"calendar"})
    List<UserCalendarEntity> findByUserEntity(UserEntity userEntity);

}
