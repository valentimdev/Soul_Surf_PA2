package com.soulsurf.backend.repository;

import com.soulsurf.backend.entities.Conversation;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ConversationRepository extends JpaRepository<Conversation, String> {}
