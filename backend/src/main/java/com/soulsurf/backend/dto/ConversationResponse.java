package com.soulsurf.backend.dto;

import lombok.*;

import java.time.Instant;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class ConversationResponse {
    private String id;
    private boolean group;

    // dados do outro participante (para DM)
    private String otherUserId;
    private String otherUserName;
    private String otherUserAvatarUrl;

    // preview da última mensagem
    private ChatMessagePreview lastMessage;

    // não lidas (simplificado)
    private Integer unreadCount = 0;

    @Data
    public static class ChatMessagePreview {
        private String senderId;
        private String content;
        private Instant createdAt;
    }
}
