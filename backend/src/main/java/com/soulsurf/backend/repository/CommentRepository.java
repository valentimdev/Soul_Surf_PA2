package com.soulsurf.backend.repository;

import com.soulsurf.backend.entities.Comment;
import com.soulsurf.backend.entities.Post;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CommentRepository extends JpaRepository<Comment, Long> {
    List<Comment> findByPostOrderByDataDesc(Post post);

    List<Comment> findByPostAndParentCommentIsNullOrderByDataDesc(Post post);
}