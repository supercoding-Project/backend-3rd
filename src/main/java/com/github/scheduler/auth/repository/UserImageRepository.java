package com.github.scheduler.auth.repository;

import com.github.scheduler.auth.entity.UserEntity;
import com.github.scheduler.auth.entity.UserImageEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserImageRepository extends JpaRepository<UserImageEntity, Long> {
    UserImageEntity findByUserEntity(UserEntity userEntity);
    UserImageEntity findByUserEntity_Email(String email);
}
