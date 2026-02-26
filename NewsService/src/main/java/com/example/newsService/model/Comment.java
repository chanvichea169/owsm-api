package com.example.newsService.model;

import com.example.newsService.audit.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "tbl_comments")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Comment extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "news_id")
    private News news;

    private String userName;
    private String userEmail;
    private String content;
    private Boolean isApproved = false;
}