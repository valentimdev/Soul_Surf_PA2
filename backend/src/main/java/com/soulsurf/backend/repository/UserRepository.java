package com.soulsurf.backend.repository;

import com.soulsurf.backend.entities.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByEmail(String email);

    Optional<User> findById(Long id);
    Optional<User> findByUsername(String username);

    // Buscar usuários por nome ou username (sugestões para menções)
    List<User> findByUsernameContainingIgnoreCase(String username);

    // Buscar usuários que o usuário atual segue e cujo nome contém o termo de busca
    @Query("SELECT u FROM User u WHERE u IN (SELECT s FROM User me JOIN me.seguindo s WHERE me.id = :userId) AND LOWER(u.username) LIKE LOWER(CONCAT('%', :searchTerm, '%'))")
    List<User> findFollowedUsersContainingUsername(Long userId, String searchTerm);
    long countByAdminTrue();
    long countByBannedTrue();
}