package com.github.scheduler.admin.repository;

import com.github.scheduler.admin.dto.inquiry.InquiryListResponseDTO;
import com.github.scheduler.admin.entity.InquiryEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface InquiryRepository extends JpaRepository<InquiryEntity, Long> {

    @Query("SELECT i FROM InquiryEntity i WHERE :keyword IS NULL OR i.title LIKE %:keyword%")
    Page<InquiryEntity> findByTitleKeyword(@Param("keyword") String keyword, Pageable pageable);
}
