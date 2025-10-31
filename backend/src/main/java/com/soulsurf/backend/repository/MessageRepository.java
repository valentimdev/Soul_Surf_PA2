package com.soulsurf.backend.repository;

import com.soulsurf.backend.entities.Message;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MessageRepository extends JpaRepository<Message, String> {
    Page<Message> findByConversationIdOrderByCreatedAtDesc(String conversationId, Pageable pageable);
    Message findTop1ByConversationIdOrderByCreatedAtDesc(String conversationId);
}
