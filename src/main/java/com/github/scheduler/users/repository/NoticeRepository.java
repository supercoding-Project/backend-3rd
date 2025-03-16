package com.github.scheduler.users.repository;

import com.github.scheduler.admin.entity.NoticeEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface NoticeRepository extends JpaRepository<NoticeEntity,Long> {
}
