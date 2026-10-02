package com.example.newsService.repository;

import com.example.newsService.model.Comment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface CommentRepository extends JpaRepository<Comment, Long> {

    @Modifying
    @Query("delete from Comment comment where comment.news.id = :newsId")
    void deleteAllByNewsId(@Param("newsId") Long newsId);
}
