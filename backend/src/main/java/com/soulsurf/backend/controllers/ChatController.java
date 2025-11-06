package com.soulsurf.backend.controllers;

import com.soulsurf.backend.dto.ChatMessageResponse;
import com.soulsurf.backend.dto.ConversationResponse;
import com.soulsurf.backend.dto.CreateDMRequest;
import com.soulsurf.backend.dto.SendMessageRequest;
import com.soulsurf.backend.entities.ConversationParticipantId;
import com.soulsurf.backend.entities.Message;
import com.soulsurf.backend.repository.ConversationParticipantRepository;
import com.soulsurf.backend.repository.MessageRepository;
import com.soulsurf.backend.repository.UserRepository;
import com.soulsurf.backend.security.AuthUtils;
import com.soulsurf.backend.services.ChatService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import jakarta.validation.Valid;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.util.List;
import java.util.Map;

@RestController
@Validated
@RequestMapping("/api/chat")
public class ChatController {
    private final ChatService chat;

    // repos para enriquecer a resposta
    private final ConversationParticipantRepository partRepo;
    private final MessageRepository msgRepo;
    private final UserRepository userRepo;

    @Autowired
    private SimpMessagingTemplate messagingTemplate;

    public ChatController(ChatService chat,
                          ConversationParticipantRepository partRepo,
                          MessageRepository msgRepo,
                          UserRepository userRepo) {
        this.chat = chat;
        this.partRepo = partRepo;
        this.msgRepo = msgRepo;
        this.userRepo = userRepo;
    }

    @PostMapping("/dm")
    public Map<String,String> createOrGetDM(@RequestBody CreateDMRequest req) {
        var me = AuthUtils.currentUserId();
        var conv = chat.ensureDM(me, req.getOtherUserId());
        return Map.of("conversationId", conv.getId());
    }

    // ====== ENRIQUECIDO ======
    @GetMapping("/conversations")
    public List<ConversationResponse> myConversations() {
        var me = AuthUtils.currentUserId();

        return chat.listForUser(me).stream().map(c -> {
            var r = new ConversationResponse();
            r.setId(c.getId());
            r.setGroup(c.isGroup());

            // Outro participante (DM)
            if (!c.isGroup()) {
                var otherId = partRepo.findOtherUserId(c.getId(), me);
                r.setOtherUserId(otherId);

                if (otherId != null) {
                    userRepo.findByEmail(otherId).ifPresent(u -> {
                        // Usa os campos que você realmente tem no UserDTO/User
                        r.setOtherUserName(u.getUsername());
                        r.setOtherUserAvatarUrl(u.getFotoPerfil());
                    });
                }
            }



            // Última mensagem (preview)
            var last = msgRepo.findTop1ByConversationIdOrderByCreatedAtDesc(c.getId());
            if (last != null) {
                var p = new ConversationResponse.ChatMessagePreview();
                p.setSenderId(last.getSenderId());
                p.setContent(last.getContent());
                p.setCreatedAt(last.getCreatedAt());
                r.setLastMessage(p);
            }

            // Não lidas (simplificado): se last.createdAt > lastReadAt do participante → 1
            partRepo.findById(new ConversationParticipantId(c.getId(), me)).ifPresent(part -> {
                Instant lastRead = part.getLastReadAt();
                if (last != null && (lastRead == null || last.getCreatedAt().isAfter(lastRead))) {
                    r.setUnreadCount(1);
                } else {
                    r.setUnreadCount(0);
                }
            });

            return r;
        }).toList();
    }

    @GetMapping("/conversations/{id}/messages")
    public List<ChatMessageResponse> listMessages(@PathVariable String id,
                                                  @RequestParam(defaultValue="0") int page,
                                                  @RequestParam(defaultValue="30") int size) {
        var me = AuthUtils.currentUserId();
        // opcional: validar participação
        var msgs = chat.listMessages(id, page, size);
        return msgs.getContent().stream().map(this::toResp).toList();
    }

    @PostMapping("/conversations/{id}/messages")
    public ChatMessageResponse send(@PathVariable String id, @Valid @RequestBody SendMessageRequest req) {
        var me = AuthUtils.currentUserId();
        if ((req.getContent() == null || req.getContent().isBlank()) && (req.getAttachmentUrl() == null || req.getAttachmentUrl().isBlank())) {
            throw new IllegalArgumentException("Mensagem vazia: informe conteúdo ou anexo");
        }
        Message saved = chat.sendMessage(id, me, req.getContent(), req.getAttachmentUrl());
        ChatMessageResponse payload = toResp(saved);
        // WebSocket
        messagingTemplate.convertAndSend("/topic/conversations/" + id, payload);
        return payload;
    }

    private ChatMessageResponse toResp(Message m) {
        var r = new ChatMessageResponse();
        r.setId(m.getId());
        r.setConversationId(m.getConversationId());
        r.setSenderId(m.getSenderId());
        r.setContent(m.getContent());
        r.setAttachmentUrl(m.getAttachmentUrl());
        r.setCreatedAt(m.getCreatedAt());
        r.setEditedAt(m.getEditedAt());
        return r;
    }
}
