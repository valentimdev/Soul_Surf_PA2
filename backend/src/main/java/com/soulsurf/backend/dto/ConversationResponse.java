package com.soulsurf.backend.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ConversationResponse {
    private String id;
    private boolean isGroup;
    private String lastMessagePreview;
    private Instant lastMessageAt;
}
