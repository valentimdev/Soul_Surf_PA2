package com.soulsurf.backend.repository;

import com.soulsurf.backend.entities.Beach;
import com.soulsurf.backend.entities.Post;
import com.soulsurf.backend.entities.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface PostRepository extends JpaRepository<Post, Long> {
    List<Post> findByPublicoIsTrueOrderByDataDesc();
    List<Post> findByUsuarioOrderByDataDesc(User usuario);
    List<Post> findByBeachOrderByDataDesc(Beach beach);

    List<Post> findByUsuarioInOrderByDataDesc(List<User> usuarios);

    @Query("SELECT p FROM Post p WHERE p.beach = ?1 AND (p.publico = true OR p.usuario.email = ?2)")
    Page<Post> findByBeachAndPublicoIsTrueOrUsuarioEmail(Beach beach, String userEmail, Pageable pageable);

    Page<Post> findByBeachAndPublicoIsTrue(Beach beach, Pageable pageable);
}