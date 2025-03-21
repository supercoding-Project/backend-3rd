package com.github.scheduler.admin.repository;

import com.github.scheduler.admin.entity.FaqCategory;
import com.github.scheduler.admin.entity.FaqEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;



@Repository
public interface FaqRepository extends JpaRepository<FaqEntity, Long> {

    List<FaqEntity> findByCategory(FaqCategory category);
}
