package com.github.scheduler.auth.repository;

import com.github.scheduler.auth.entity.UserEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<UserEntity, Long> {
    Optional<UserEntity> findByUserId(Long userId);
    Optional<UserEntity> findByEmail(String email);
    Optional<UserEntity> findByUsername(String username);
    boolean existsByEmail(String email);
}
