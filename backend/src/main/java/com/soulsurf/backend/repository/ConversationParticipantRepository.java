// com.soulsurf.backend.repository.ConversationParticipantRepository
package com.soulsurf.backend.repository;

import com.soulsurf.backend.entities.ConversationParticipant;
import com.soulsurf.backend.entities.ConversationParticipantId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface ConversationParticipantRepository extends JpaRepository<ConversationParticipant, ConversationParticipantId> {

    List<ConversationParticipant> findAllByUserId(String userId);

    @Query(value = """
  select cp.conversation_id
  from conversation_participants cp
  where cp.user_id in (?1, ?2)
  group by cp.conversation_id
  having count(distinct cp.user_id) = 2
  limit 1
""", nativeQuery = true)
    String findDMBetween(String userA, String userB);


    @Query(value = """
      select user_id from conversation_participants
      where conversation_id = :cid and user_id <> :me
      limit 1
    """, nativeQuery = true)
    String findOtherUserId(String cid, String me);

    @Query(value = "select * from conversation_participants where conversation_id = :cid", nativeQuery = true)
    List<ConversationParticipant> findAllByConversationId(String cid);
}
