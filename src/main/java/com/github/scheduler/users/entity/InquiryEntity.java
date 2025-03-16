package com.github.scheduler.users.entity;

import com.github.scheduler.admin.entity.InquiryAnswerEntity;
import com.github.scheduler.auth.entity.UserEntity;
import jakarta.persistence.*;

import java.time.LocalDateTime;
import java.util.List;

@Entity
public class InquiryEntity {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String title;

    private String content;

    private LocalDateTime createdAt;

    private boolean answered;   // 답변 여부

    private String password;

    private boolean isPrivate;  // 공개 여부

    @Enumerated(EnumType.STRING)
    private InquiryCategory category;

    @ManyToOne
    private UserEntity user;

    @OneToMany(mappedBy = "inquiry" , cascade = CascadeType.ALL)
    private List<InquiryAnswerEntity> answers;

    public void markAsAnswered() {
        this.answered = true;
    }



}
