package com.example.newsService.model;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "tbl_tags")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Tag {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;
    private String slug;

    @ManyToMany(mappedBy = "tags")
    private List<News> newsList;

    private LocalDateTime createdAt;

    @PrePersist
    public void prePersist() { createdAt = LocalDateTime.now(); }
}