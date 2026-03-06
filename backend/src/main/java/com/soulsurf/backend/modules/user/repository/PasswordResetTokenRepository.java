package com.soulsurf.backend.modules.user.repository;

import com.soulsurf.backend.modules.user.entity.PasswordResetToken;
import com.soulsurf.backend.modules.user.entity.User;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface PasswordResetTokenRepository extends JpaRepository<PasswordResetToken, Long> {
    Optional<PasswordResetToken> findByToken(String token);

    Optional<PasswordResetToken> findByUser(User user);
}
