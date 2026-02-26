package com.example.newsService.model;

import com.example.newsService.audit.BaseEntity;
import jakarta.persistence.*;
import lombok.*;


@Entity
@Table(name = "tbl_media_assets")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MediaAsset extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "news_id")
    private News news;

    private String fileUrl;
    private String fileType;
    private Long fileSize;
}