package com.soulsurf.backend.controllers;

import com.soulsurf.backend.dto.*;
import com.soulsurf.backend.entities.Message;
import com.soulsurf.backend.security.AuthUtils;
import com.soulsurf.backend.services.ChatService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import jakarta.validation.Valid;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@Validated
@RequestMapping("/api/chat")
public class ChatController {
    private final ChatService chat;
    public ChatController(ChatService chat) { this.chat = chat; }

    @PostMapping("/dm")
    public Map<String,String> createOrGetDM(@RequestBody CreateDMRequest req) {
        var me = AuthUtils.currentUserId();
        var conv = chat.ensureDM(me, req.getOtherUserId());
        return Map.of("conversationId", conv.getId());
    }

    @GetMapping("/conversations")
    public List<ConversationResponse> myConversations() {
        var me = AuthUtils.currentUserId();
        return chat.listForUser(me).stream().map(c -> {
            var r = new ConversationResponse();
            r.setId(c.getId()); r.setGroup(c.isGroup());
            // (opcional) popular preview consultando última mensagem
            return r;
        }).toList();
    }

    @GetMapping("/conversations/{id}/messages")
    public List<ChatMessageResponse> listMessages(@PathVariable String id,
                                                  @RequestParam(defaultValue="0") int page,
                                                  @RequestParam(defaultValue="30") int size) {
        var me = AuthUtils.currentUserId();
        // (opcional) verificação se me participa da conversa
        var msgs = chat.listMessages(id, page, size);
        return msgs.getContent().stream().map(m -> {
            var r = new ChatMessageResponse();
            r.setId(m.getId()); r.setConversationId(m.getConversationId()); r.setSenderId(m.getSenderId());
            r.setContent(m.getContent()); r.setAttachmentUrl(m.getAttachmentUrl());
            r.setCreatedAt(m.getCreatedAt()); r.setEditedAt(m.getEditedAt());
            return r;
        }).toList();
    }

    @PostMapping("/conversations/{id}/messages")
    public ChatMessageResponse send(@PathVariable String id, @Valid @RequestBody SendMessageRequest req) {
        var me = AuthUtils.currentUserId();
        if ((req.getContent() == null || req.getContent().isBlank()) && (req.getAttachmentUrl() == null || req.getAttachmentUrl().isBlank())) {
            throw new IllegalArgumentException("Mensagem vazia: informe conteúdo ou anexo");
        }
        Message saved = chat.sendMessage(id, me, req.getContent(), req.getAttachmentUrl());
        ChatMessageResponse payload = toResp(saved);

        // 🔔 Notifica todos conectados na conversa em tempo real

        messagingTemplate.convertAndSend("/topic/conversations/" + id, payload);
        return payload;
    }


    @Autowired
    private SimpMessagingTemplate messagingTemplate;

    private ChatMessageResponse toResp(Message m) {
        var r = new ChatMessageResponse();
        r.setId(m.getId()); r.setConversationId(m.getConversationId()); r.setSenderId(m.getSenderId());
        r.setContent(m.getContent()); r.setAttachmentUrl(m.getAttachmentUrl());
        r.setCreatedAt(m.getCreatedAt()); r.setEditedAt(m.getEditedAt());
        return r;
    }
}

