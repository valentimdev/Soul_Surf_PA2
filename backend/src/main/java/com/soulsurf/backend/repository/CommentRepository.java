package com.soulsurf.backend.repository;

import com.soulsurf.backend.entities.Comment;
import com.soulsurf.backend.entities.Post;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface CommentRepository extends JpaRepository<Comment, Long> {

    // Comentários de um post, ordenados pela data (decrescente)
    List<Comment> findByPostOrderByDataDesc(Post post);

    // Comentários raiz (sem comentário pai), ordenados por data (decrescente)
    List<Comment> findByPostAndParentCommentIsNullOrderByDataDesc(Post post);

    // Contar comentários entre dois instantes de tempo
    @Query("SELECT COUNT(c) FROM Comment c WHERE c.data BETWEEN :start AND :end")
    long countCommentsBetween(@Param("start") java.time.LocalDateTime start,
                              @Param("end") java.time.LocalDateTime end);
}
