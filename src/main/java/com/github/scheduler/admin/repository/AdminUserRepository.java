package com.github.scheduler.admin.repository;

import com.github.scheduler.auth.entity.UserEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AdminUserRepository extends JpaRepository<UserEntity,Long> {
}
