package com.github.scheduler.auth.repository;

import com.github.scheduler.auth.entity.RefreshTokenEntity;
import com.github.scheduler.auth.entity.UserEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface RefreshTokenRepository extends JpaRepository<RefreshTokenEntity, Long> {
    Optional<RefreshTokenEntity> findByUserEntity(UserEntity userEntity);
}
