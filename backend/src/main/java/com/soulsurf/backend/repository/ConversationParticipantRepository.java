package com.soulsurf.backend.repository;

import com.soulsurf.backend.entities.ConversationParticipant;
import com.soulsurf.backend.entities.ConversationParticipantId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface ConversationParticipantRepository
        extends JpaRepository<ConversationParticipant, ConversationParticipantId> {

    @Query(value = """
        select conversation_id
        from conversation_participants
        where user_id in (:a, :b)
        group by conversation_id
        having count(distinct user_id) = 2
        limit 1
        """, nativeQuery = true)
    Optional<String> findDMConversationId(String a, String b);

    @Query(value = "select * from conversation_participants where user_id = :userId", nativeQuery = true)
    List<ConversationParticipant> findAllByUserId(String userId);
}
