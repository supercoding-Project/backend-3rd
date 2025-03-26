package com.github.scheduler.admin.repository;

import com.github.scheduler.admin.entity.NoticeEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.nio.channels.FileChannel;

@Repository
public interface NoticeRepository extends JpaRepository<NoticeEntity,Long> {

    @Query("SELECT n FROM NoticeEntity n\n" +
            "    WHERE (:keyword IS NULL OR n.title LIKE %:keyword%)")
    Page<NoticeEntity> findKeyword(String keyword, Pageable pageable);
}
