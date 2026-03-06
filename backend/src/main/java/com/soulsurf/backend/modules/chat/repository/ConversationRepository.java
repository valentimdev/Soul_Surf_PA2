package com.soulsurf.backend.modules.chat.repository;

import com.soulsurf.backend.modules.chat.entity.Conversation;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ConversationRepository extends JpaRepository<Conversation, String> {}

