package com.github.scheduler.admin.entity;

import com.github.scheduler.auth.entity.UserEntity;
import com.github.scheduler.users.entity.InquiryEntity;
import jakarta.persistence.*;

import java.time.LocalDateTime;
import java.util.List;

@Entity
public class InquiryAnswerEntity {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String content;

    private LocalDateTime answeredAt;

    private LocalDateTime updatedAt;

    @ManyToOne
    private UserEntity admin;

    @ManyToOne
    private InquiryEntity inquiry;  // 문의글 참조

    @ManyToOne
    @JoinColumn(name = "parent_answer_id")
    private InquiryAnswerEntity parent;  // 상위 댓글

    @OneToMany(mappedBy = "parent")
    private List<InquiryAnswerEntity> replies;  // 대댓글

    @PrePersist
    @PreUpdate
    public void updateTimestamps() {
        this.answeredAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }


}
