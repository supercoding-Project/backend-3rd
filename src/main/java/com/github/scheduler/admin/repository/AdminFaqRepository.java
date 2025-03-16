package com.github.scheduler.admin.repository;

import com.github.scheduler.admin.entity.FaqEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AdminFaqRepository extends JpaRepository<FaqEntity, Long> {
}
