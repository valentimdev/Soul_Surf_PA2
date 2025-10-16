package com.soulsurf.backend.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ChatMessageResponse {
    private String id;
    private String conversationId;
    private String senderId;
    private String content;
    private String attachmentUrl;
    private Instant createdAt;
    private Instant editedAt;
}
