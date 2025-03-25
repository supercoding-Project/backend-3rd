package com.github.scheduler.admin.entity;

import com.github.scheduler.auth.entity.UserEntity;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.List;

@Entity
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "inquiry_answer")
public class InquiryAnswerEntity {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @Column(name = "content")
    private String content;

    @Column(name = "answered_at")
    private LocalDateTime answeredAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @ManyToOne
    @JoinColumn(name = "admin_id")
    private UserEntity admin;

    @ManyToOne
    @JoinColumn(name = "inquiry_id")
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

    public void updateContent(String newContent) {
        this.content = newContent;
        this.updatedAt = LocalDateTime.now();
    }



}
