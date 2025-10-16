package com.soulsurf.backend.repository;

import com.soulsurf.backend.entities.Notification;
import com.soulsurf.backend.entities.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface NotificationRepository extends JpaRepository<Notification, Long> {
    List<Notification> findByRecipientOrderByCreatedAtDesc(User recipient);
    int countByRecipientAndReadFalse(User recipient);
}
