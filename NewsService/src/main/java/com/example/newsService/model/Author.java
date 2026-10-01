package com.example.newsService.model;

import com.example.newsService.audit.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "tbl_authors")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Author extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String fullName;
    private String fullNameEn;
    private String fullNameKh;
    private String email;
    private String bio;
    private String bioEn;
    private String bioKh;
    private String avatarUrl;
    private Boolean isActive = true;
}