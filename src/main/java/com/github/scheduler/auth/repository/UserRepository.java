package com.github.scheduler.auth.repository;

import com.github.scheduler.auth.entity.UserEntity;
import com.jayway.jsonpath.JsonPath;
import jakarta.validation.constraints.Past;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<UserEntity, Long> {
    Optional<UserEntity> findByUserId(Long userId);
    Optional<UserEntity> findByEmail(String email);
    Optional<UserEntity> findByUsername(String username);
    boolean existsByEmail(String email);

    @Query("SELECT u FROM UserEntity u WHERE " +
            "(:keyword IS NULL OR u.username LIKE %:keyword% OR u.email LIKE %:keyword%) AND " +
            "(:start IS NULL OR u.createdAt >= :start) AND " +
            "(:end IS NULL OR u.createdAt <= :end)")
    Page<UserEntity> searchUsers(@Param("keyword") String keyword,
                                 @Param("start") LocalDateTime start,
                                 @Param("end") LocalDateTime end,
                                 Pageable pageable);
}
