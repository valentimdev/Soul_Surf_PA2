package com.soulsurf.backend.modules.chat.dto;

import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SendMessageRequest {
    @Size(max = 5000)
    private String content;
    private String attachmentUrl;
}
