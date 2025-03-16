package com.github.scheduler.admin.repository;

import com.github.scheduler.admin.entity.NoticeEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AdminNoticeRepository extends JpaRepository<NoticeEntity,Long> {
}
