package com.github.scheduler.admin.repository;

import com.github.scheduler.admin.entity.InquiryAnswerEntity;
import com.github.scheduler.admin.entity.InquiryEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface InquiryAnswerRepository extends JpaRepository<InquiryAnswerEntity,Long> {
    boolean existsByInquiry(InquiryEntity inquiry);
}
