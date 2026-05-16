package com.soulsurf.backend.modules.notification.repository;

import com.soulsurf.backend.modules.notification.entity.PushToken;
import com.soulsurf.backend.modules.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PushTokenRepository extends JpaRepository<PushToken, Long> {
    Optional<PushToken> findByToken(String token);

    List<PushToken> findByUserAndActiveTrue(User user);
}
