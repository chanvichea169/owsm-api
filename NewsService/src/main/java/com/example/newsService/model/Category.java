package com.example.newsService.model;

import com.example.newsService.audit.BaseEntity;
import jakarta.persistence.*;
import lombok.*;
import java.util.List;

@Entity
@Table(name = "tbl_categories")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Category extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;
    private String slug;
    private String description;

    @ManyToOne
    @JoinColumn(name = "parent_id")
    private Category parent;

    @OneToMany(mappedBy = "parent")
    private List<Category> children;
}