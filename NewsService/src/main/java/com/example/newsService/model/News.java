package com.example.newsService.model;

import com.example.newsService.enumeration.NewsStatus;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "tbl_news")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class News {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable=false)
    private String title;

    @Column(unique = true, nullable=false)
    private String slug;

    @Column(columnDefinition = "TEXT")
    private String content;

    @Column(nullable=false)
    private String category;

    private String coverImage;
    private String author;

    @Enumerated(EnumType.STRING)
    private NewsStatus status;

    private Boolean isFeatured = false;

    private Long viewCount = 0L;

    private LocalDateTime publishedAt;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    @PrePersist
    public void prePersist(){
        createdAt = LocalDateTime.now();
    }

    @PreUpdate
    public void preUpdate(){
        updatedAt = LocalDateTime.now();
    }
}