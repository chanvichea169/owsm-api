package com.example.newsService.model;

import com.example.newsService.audit.BaseEntity;
import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "tbl_news")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class News extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String title;
    private String slug;
    @Column(columnDefinition = "TEXT")
    private String content;

    @ManyToOne
    @JoinColumn(name = "category_id")
    private Category category;

    @ManyToOne
    @JoinColumn(name = "author_id")
    private Author author;

    @ManyToMany
    @JoinTable(
            name = "tbl_news_tags",
            joinColumns = @JoinColumn(name = "news_id"),
            inverseJoinColumns = @JoinColumn(name = "tag_id")
    )
    private List<Tag> tags;

    @OneToMany(mappedBy = "news")
    private List<Comment> comments;

    private String coverImage;
    private String status;
    private Boolean isFeatured = false;
    private Long viewCount = 0L;
    private LocalDateTime publishedAt;
}