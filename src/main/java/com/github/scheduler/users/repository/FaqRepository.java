package com.github.scheduler.users.repository;

import com.github.scheduler.admin.entity.FaqEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface FaqRepository extends JpaRepository<FaqEntity,Long> {
}
