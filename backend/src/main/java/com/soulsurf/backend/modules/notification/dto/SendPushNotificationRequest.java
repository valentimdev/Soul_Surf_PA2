package com.soulsurf.backend.modules.notification.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class SendPushNotificationRequest {

    @NotBlank
    private String targetUsername;

    @NotBlank
    private String title;

    @NotBlank
    private String body;
}
