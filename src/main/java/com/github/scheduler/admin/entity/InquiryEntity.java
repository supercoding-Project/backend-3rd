package com.github.scheduler.admin.entity;

import com.github.scheduler.admin.dto.inquiry.InquiryRequestDTO;
import com.github.scheduler.auth.entity.UserEntity;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;
import java.util.List;

@Entity
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "inquiry")
public class InquiryEntity {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @Column(name = "title")
    private String title;

    @Column(name = "content")
    private String content;

    @CreationTimestamp
    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "answered")
    private boolean answered;   // 답변 여부

    @Column(name = "password")
    private String password;

    @Column(name = "is_private")
    private boolean isPrivate;  // 공개 여부

    @Enumerated(EnumType.STRING)
    @Column(name = "category")
    private InquiryCategory category;

    @ManyToOne
    @JoinColumn(name = "user_id")
    private UserEntity user;

    @OneToMany(mappedBy = "inquiry" , cascade = CascadeType.ALL)
    private List<InquiryAnswerEntity> answers;

    public void markAsAnswered() {
        this.answered = true;
    }

    public void markAsUnanswered() {
        this.answered = false;
    }

    public static InquiryEntity create(InquiryRequestDTO dto, UserEntity user) {
        return InquiryEntity.builder()
                .title(dto.getTitle())
                .content(dto.getContent())
                .password(dto.getPassword())
                .isPrivate(dto.isPrivate())
                .category(dto.getCategory())
                .createdAt(LocalDateTime.now())
                .answered(false)
                .user(user)
                .build();
    }

    public void update(String title, String content, InquiryCategory category, boolean isPrivate, String password) {
        this.title = title;
        this.content = content;
        this.category = category;
        this.isPrivate = isPrivate;
        this.password = password;
    }



}
