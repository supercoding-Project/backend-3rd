package com.github.scheduler.auth.repository;

import com.github.scheduler.auth.entity.UserEntity;
import com.github.scheduler.auth.entity.UserImageEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserImageRepository extends JpaRepository<UserImageEntity, Long> {
    // 유저엔티티로 유저이미지 가져오기
    UserImageEntity findByUserEntity(UserEntity userEntity);
    UserImageEntity findByUserEntity_Email(String email);
}
