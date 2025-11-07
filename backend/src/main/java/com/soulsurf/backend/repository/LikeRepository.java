package com.soulsurf.backend.repository;

import com.soulsurf.backend.entities.Like;
import com.soulsurf.backend.entities.Post;
import com.soulsurf.backend.entities.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface LikeRepository extends JpaRepository<Like, Long> {
    Optional<Like> findByPostAndUsuario(Post post, User usuario);
    boolean existsByPostAndUsuario(Post post, User usuario);
    long countByPost(Post post);
    void deleteByPostAndUsuario(Post post, User usuario);
}

