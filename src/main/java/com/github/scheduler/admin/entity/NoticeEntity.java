package com.github.scheduler.admin.entity;

import jakarta.persistence.*;

import java.time.LocalDate;

@Entity
public class NoticeEntity {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String title;

    private String content;

    private LocalDate createdAt;


}
