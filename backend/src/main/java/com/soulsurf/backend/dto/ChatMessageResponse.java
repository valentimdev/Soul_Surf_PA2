package com.soulsurf.backend.dto;

import lombok.*;

import java.time.Instant;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class ChatMessageResponse {
    private String id;
    private String conversationId;
    private String senderId;
    private String content;
    private String attachmentUrl;
    private Instant createdAt;
    private Instant editedAt;
}