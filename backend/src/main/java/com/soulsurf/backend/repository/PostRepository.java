package com.soulsurf.backend.repository;

import com.soulsurf.backend.entities.Post;
import com.soulsurf.backend.entities.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PostRepository extends JpaRepository<Post, Long> {
    List<Post> findByPublicoIsTrueOrderByDataDesc();
    List<Post> findByUsuarioOrderByDataDesc(User usuario);
}