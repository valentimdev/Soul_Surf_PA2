package com.soulsurf.backend.entities;
import jakarta.persistence.*;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import java.time.Instant;

@Entity @Table(name = "conversation_participants")
@IdClass(ConversationParticipantId.class)
@Getter
@Setter
@NoArgsConstructor
public class ConversationParticipant {
    @Id @Column(name="conversation_id") private String conversationId;
    @Id @Column(name="user_id") private String userId;
    @Column(name="last_read_at") private Instant lastReadAt;
    // getters/setters
}
