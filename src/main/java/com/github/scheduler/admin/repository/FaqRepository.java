package com.github.scheduler.admin.repository;

import com.github.scheduler.admin.entity.FaqCategory;
import com.github.scheduler.admin.entity.FaqEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;



@Repository
public interface FaqRepository extends JpaRepository<FaqEntity, Long> {

    Page<FaqEntity> findByCategory(FaqCategory category, Pageable pageable);

    @Query("SELECT f FROM FaqEntity f WHERE (:keyword IS NULL OR f.question LIKE %:keyword%)")
    Page<FaqEntity> findByKeyword(@Param("keyword") String keyword, Pageable pageable);
}
