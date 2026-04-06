// com.soulsurf.backend.repository.ConversationParticipantRepository
package com.soulsurf.backend.modules.chat.repository;

import com.soulsurf.backend.modules.chat.entity.ConversationParticipant;
import com.soulsurf.backend.modules.chat.entity.ConversationParticipantId;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface ConversationParticipantRepository extends JpaRepository<ConversationParticipant, ConversationParticipantId> {

    List<ConversationParticipant> findAllByUserId(String userId);

    @Query("""
            select cp.conversationId
            from ConversationParticipant cp
            where cp.userId in (?1, ?2)
            group by cp.conversationId
            having count(distinct cp.userId) = 2
            """)
    List<String> findDMBetweenCandidates(String userA, String userB, Pageable pageable);

    default String findDMBetween(String userA, String userB) {
        return findDMBetweenCandidates(userA, userB, PageRequest.of(0, 1))
                .stream()
                .findFirst()
                .orElse(null);
    }

    Optional<ConversationParticipant> findFirstByConversationIdAndUserIdNot(String cid, String me);

    default String findOtherUserId(String cid, String me) {
        return findFirstByConversationIdAndUserIdNot(cid, me)
                .map(ConversationParticipant::getUserId)
                .orElse(null);
    }

    List<ConversationParticipant> findAllByConversationId(String cid);
}

