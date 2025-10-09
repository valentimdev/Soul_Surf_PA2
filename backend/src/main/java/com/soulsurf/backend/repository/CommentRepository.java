package com.soulsurf.backend.repository;

import com.soulsurf.backend.entities.Comment;
import com.soulsurf.backend.entities.Post;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface CommentRepository extends JpaRepository<Comment, Long> {
    List<Comment> findByPostOrderByDataDesc(Post post);

    @Query("SELECT COUNT(c) FROM Comment c WHERE c.data BETWEEN :start AND :end")
    long countCommentsBetween(@Param("start") java.time.LocalDateTime start,
                              @Param("end") java.time.LocalDateTime end);
}