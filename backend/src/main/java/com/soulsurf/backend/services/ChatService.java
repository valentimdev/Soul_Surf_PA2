package com.soulsurf.backend.services;

import com.soulsurf.backend.entities.Conversation;
import com.soulsurf.backend.entities.ConversationParticipant;
import com.soulsurf.backend.entities.ConversationParticipantId;
import com.soulsurf.backend.entities.Message;
import com.soulsurf.backend.repository.ConversationParticipantRepository;
import com.soulsurf.backend.repository.ConversationRepository;
import com.soulsurf.backend.repository.MessageRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;

@Service
public class ChatService {
    private final ConversationRepository convRepo;
    private final ConversationParticipantRepository partRepo;
    private final MessageRepository msgRepo;

    public ChatService(ConversationRepository c, ConversationParticipantRepository p, MessageRepository m) {
        this.convRepo = c; this.partRepo = p; this.msgRepo = m;
    }

    @Transactional
    public Conversation ensureDM(String userA, String userB) {
        if (userA.equals(userB)) throw new RuntimeException("DM precisa de usuários distintos");

        // Tenta achar conversa existente
        String existingId = partRepo.findDMBetween(userA, userB);

        if (existingId != null) {
            return convRepo.findById(existingId)
                    .orElseThrow(() -> new RuntimeException("Conversa existente não encontrada"));
        }

        // Cria nova
        var conv = new Conversation();
        conv.setGroup(false);
        conv = convRepo.save(conv);

        var pa = new ConversationParticipant();
        pa.setConversationId(conv.getId());
        pa.setUserId(userA);

        var pb = new ConversationParticipant();
        pb.setConversationId(conv.getId());
        pb.setUserId(userB);

        partRepo.saveAll(List.of(pa, pb));
        return conv;
    }


    public List<Conversation> listForUser(String userId) {
        var parts = partRepo.findAllByUserId(userId);
        var ids = parts.stream().map(ConversationParticipant::getConversationId).toList();
        return ids.isEmpty()? List.of() : convRepo.findAllById(ids);
    }

    @Transactional
    public Message sendMessage(String conversationId, String senderId, String content, String attachmentUrl) {
        // valida se o remetente participa da conversa
        var participantId = new ConversationParticipantId();
        participantId.setConversationId(conversationId);
        participantId.setUserId(senderId);
        if (partRepo.findById(participantId).isEmpty()) {
            throw new org.springframework.security.access.AccessDeniedException("Usuário não participa da conversa");
        }

        var msg = new Message();
        msg.setConversationId(conversationId);
        msg.setSenderId(senderId);
        msg.setContent(content);
        msg.setAttachmentUrl(attachmentUrl);
        msg = msgRepo.save(msg);
        // atualizar last_read do remetente, opcional
        partRepo.findById(participantId).ifPresent(p -> { p.setLastReadAt(Instant.now()); partRepo.save(p); });
        return msg;
    }

    public Page<Message> listMessages(String conversationId, int page, int size) {
        return msgRepo.findByConversationIdOrderByCreatedAtDesc(conversationId, PageRequest.of(page, size));
    }
}
