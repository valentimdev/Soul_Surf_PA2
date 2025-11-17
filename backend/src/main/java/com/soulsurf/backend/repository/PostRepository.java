package com.soulsurf.backend.repository;

import com.soulsurf.backend.entities.Beach;
import com.soulsurf.backend.entities.Post;
import com.soulsurf.backend.entities.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface PostRepository extends JpaRepository<Post, Long> {
    List<Post> findByPublicoIsTrueOrderByDataDesc();
    List<Post> findByUsuarioOrderByDataDesc(User usuario);
    List<Post> findByBeachOrderByDataDesc(Beach beach);

    List<Post> findByUsuarioInOrderByDataDesc(List<User> usuarios);

    Page<Post> findByPublicoIsTrue(Pageable pageable);
    Page<Post> findByUsuario(User usuario, Pageable pageable);
    Page<Post> findByUsuarioIn(List<User> usuarios, Pageable pageable);

    @Query("SELECT p FROM Post p WHERE p.beach = ?1 AND (p.publico = true OR p.usuario.email = ?2)")
    Page<Post> findByBeachAndPublicoIsTrueOrUsuarioEmail(Beach beach, String userEmail, Pageable pageable);

    Page<Post> findByBeachAndPublicoIsTrue(Beach beach, Pageable pageable);

    @Query("SELECT COUNT(DISTINCT p.usuario.id) FROM Post p")
    long countDistinctAuthors();

    @Query("SELECT COUNT(p) FROM Post p WHERE p.data BETWEEN :start AND :end")
    long countPostsBetween(@Param("start") java.time.LocalDateTime start,
                           @Param("end") java.time.LocalDateTime end);

    @Query("SELECT p.usuario.id, p.usuario.username, COUNT(p) FROM Post p WHERE p.data BETWEEN :start AND :end GROUP BY p.usuario.id, p.usuario.username ORDER BY COUNT(p) DESC")
    java.util.List<Object[]> topAuthorsBetween(@Param("start") java.time.LocalDateTime start,
                                               @Param("end") java.time.LocalDateTime end,
                                               org.springframework.data.domain.Pageable pageable);

    @Query("SELECT p.beach.id, p.beach.nome, COUNT(p) FROM Post p WHERE p.data BETWEEN :start AND :end AND p.beach IS NOT NULL GROUP BY p.beach.id, p.beach.nome ORDER BY COUNT(p) DESC")
    java.util.List<Object[]> postsByBeachBetween(@Param("start") java.time.LocalDateTime start,
                                                 @Param("end") java.time.LocalDateTime end);
}