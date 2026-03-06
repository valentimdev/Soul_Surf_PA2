package com.soulsurf.backend.modules.post.repository;

import com.soulsurf.backend.modules.post.entity.Like;
import com.soulsurf.backend.modules.post.entity.Post;
import com.soulsurf.backend.modules.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface LikeRepository extends JpaRepository<Like, Long> {
    Optional<Like> findByPostAndUsuario(Post post, User usuario);

    boolean existsByPostAndUsuario(Post post, User usuario);

    long countByPost(Post post);

    void deleteByPostAndUsuario(Post post, User usuario);

    void deleteAllByPost(Post post);
}

