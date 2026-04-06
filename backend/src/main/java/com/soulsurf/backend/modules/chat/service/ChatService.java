package com.soulsurf.backend.modules.chat.service;

import com.soulsurf.backend.modules.chat.entity.Conversation;
import com.soulsurf.backend.modules.chat.entity.ConversationParticipant;
import com.soulsurf.backend.modules.chat.entity.ConversationParticipantId;
import com.soulsurf.backend.modules.chat.entity.Message;
import com.soulsurf.backend.modules.chat.repository.ConversationParticipantRepository;
import com.soulsurf.backend.modules.chat.repository.ConversationRepository;
import com.soulsurf.backend.modules.chat.repository.MessageRepository;
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

    public ChatService(
            ConversationRepository convRepo,
            ConversationParticipantRepository partRepo,
            MessageRepository msgRepo) {
        this.convRepo = convRepo;
        this.partRepo = partRepo;
        this.msgRepo = msgRepo;
    }

    @Transactional
    public Conversation ensureDM(String userA, String userB) {
        if (userA.equals(userB)) {
            throw new RuntimeException("DM precisa de usuarios distintos");
        }

        String existingId = partRepo.findDMBetween(userA, userB);
        if (existingId != null) {
            return convRepo.findById(existingId)
                    .orElseThrow(() -> new RuntimeException("Conversa existente nao encontrada"));
        }

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
        return ids.isEmpty() ? List.of() : convRepo.findAllById(ids);
    }

    @Transactional
    public Message sendMessage(String conversationId, String senderId, String content, String attachmentUrl) {
        assertParticipant(conversationId, senderId);

        var participantId = new ConversationParticipantId(conversationId, senderId);

        var msg = new Message();
        msg.setConversationId(conversationId);
        msg.setSenderId(senderId);
        msg.setContent(content);
        msg.setAttachmentUrl(attachmentUrl);
        msg = msgRepo.save(msg);

        partRepo.findById(participantId).ifPresent(p -> {
            p.setLastReadAt(Instant.now());
            partRepo.save(p);
        });

        return msg;
    }

    public Page<Message> listMessages(String conversationId, String userId, int page, int size) {
        assertParticipant(conversationId, userId);
        return msgRepo.findByConversationIdOrderByCreatedAtDesc(conversationId, PageRequest.of(page, size));
    }

    private void assertParticipant(String conversationId, String userId) {
        var participantId = new ConversationParticipantId(conversationId, userId);
        if (partRepo.findById(participantId).isEmpty()) {
            throw new org.springframework.security.access.AccessDeniedException("Usuario nao participa da conversa");
        }
    }
}
