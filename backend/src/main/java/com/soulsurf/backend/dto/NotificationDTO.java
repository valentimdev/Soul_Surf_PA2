package com.soulsurf.backend.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
public class NotificationDTO {
    private Long id;
    private UserDTO sender;
    private String type;
    private Long postId;
    private Long commentId;
    private boolean read;
    private LocalDateTime createdAt;
    private String message;
}
