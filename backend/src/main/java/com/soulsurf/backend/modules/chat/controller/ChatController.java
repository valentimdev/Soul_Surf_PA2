package com.soulsurf.backend.modules.chat.controller;

import com.soulsurf.backend.modules.chat.dto.ChatMessageResponse;
import com.soulsurf.backend.modules.chat.dto.ConversationResponse;
import com.soulsurf.backend.modules.chat.dto.CreateDMRequest;
import com.soulsurf.backend.modules.chat.dto.SendMessageRequest;
import com.soulsurf.backend.modules.chat.entity.ConversationParticipantId;
import com.soulsurf.backend.modules.chat.entity.Message;
import com.soulsurf.backend.modules.chat.repository.ConversationParticipantRepository;
import com.soulsurf.backend.modules.chat.repository.MessageRepository;
import com.soulsurf.backend.modules.user.repository.UserRepository;
import com.soulsurf.backend.core.security.AuthUtils;
import com.soulsurf.backend.modules.chat.service.ChatService;
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
    private final ConversationParticipantRepository partRepo;
    private final MessageRepository msgRepo;
    private final UserRepository userRepo;
    private final SimpMessagingTemplate messagingTemplate;

    public ChatController(ChatService chat,
            ConversationParticipantRepository partRepo,
            MessageRepository msgRepo,
            UserRepository userRepo,
            SimpMessagingTemplate messagingTemplate) {
        this.chat = chat;
        this.partRepo = partRepo;
        this.msgRepo = msgRepo;
        this.userRepo = userRepo;
        this.messagingTemplate = messagingTemplate;
    }

    @PostMapping("/dm")
    public Map<String, String> createOrGetDM(@RequestBody CreateDMRequest req) {
        var me = AuthUtils.currentUserId();
        var otherUserKey = resolveUserKey(req.getOtherUserId());
        var conv = chat.ensureDM(me, otherUserKey);
        return Map.of("conversationId", conv.getId());
    }

    @GetMapping("/conversations")
    public List<ConversationResponse> myConversations() {
        var me = AuthUtils.currentUserId();

        return chat.listForUser(me).stream().map(c -> {
            var r = new ConversationResponse();
            r.setId(c.getId());
            r.setGroup(c.isGroup());

            if (!c.isGroup()) {
                var otherId = partRepo.findOtherUserId(c.getId(), me);
                r.setOtherUserId(toClientUserId(otherId));

                if (otherId != null) {
                    userRepo.findByEmail(otherId).ifPresent(u -> {
                        r.setOtherUserName(u.getUsername());
                        r.setOtherUserAvatarUrl(u.getFotoPerfil());
                    });
                }
            }

            var last = msgRepo.findTop1ByConversationIdOrderByCreatedAtDesc(c.getId());
            if (last != null) {
                var p = new ConversationResponse.ChatMessagePreview();
                p.setSenderId(toClientUserId(last.getSenderId()));
                p.setContent(last.getContent());
                p.setCreatedAt(last.getCreatedAt());
                r.setLastMessage(p);
            }

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
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "30") int size) {
        var me = AuthUtils.currentUserId();
        var msgs = chat.listMessages(id, me, page, size);
        return msgs.getContent().stream().map(this::toResp).toList();
    }

    @PostMapping("/conversations/{id}/messages")
    public ChatMessageResponse send(@PathVariable String id, @Valid @RequestBody SendMessageRequest req) {
        var me = AuthUtils.currentUserId();

        Message saved = chat.sendMessage(id, me, req.getContent(), req.getAttachmentUrl());
        ChatMessageResponse payload = toResp(saved);

        messagingTemplate.convertAndSend("/topic/conversations/" + id, payload);

        return payload;
    }

    private ChatMessageResponse toResp(Message m) {
        var r = new ChatMessageResponse();
        r.setId(m.getId());
        r.setConversationId(m.getConversationId());
        r.setSenderId(toClientUserId(m.getSenderId()));
        r.setContent(m.getContent());
        r.setAttachmentUrl(m.getAttachmentUrl());
        r.setCreatedAt(m.getCreatedAt());
        r.setEditedAt(m.getEditedAt());
        return r;
    }

    private String resolveUserKey(String otherUserId) {
        if (otherUserId == null || otherUserId.isBlank()) {
            throw new IllegalArgumentException("otherUserId obrigatorio");
        }

        // 1) Cliente enviou email (chave interna atual do chat)
        if (userRepo.findByEmail(otherUserId).isPresent()) {
            return otherUserId;
        }

        // 2) Cliente enviou ID numerico do usuario
        try {
            long numericId = Long.parseLong(otherUserId);
            return userRepo.findById(numericId)
                    .map(u -> u.getEmail())
                    .orElseThrow(() -> new IllegalArgumentException("Usuario destino nao encontrado"));
        } catch (NumberFormatException ex) {
            throw new IllegalArgumentException("otherUserId invalido");
        }
    }

    private String toClientUserId(String internalUserKey) {
        if (internalUserKey == null || internalUserKey.isBlank()) {
            return internalUserKey;
        }

        return userRepo.findByEmail(internalUserKey)
                .map(u -> String.valueOf(u.getId()))
                .orElse(internalUserKey);
    }
}
