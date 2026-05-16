package com.soulsurf.backend.modules.notification.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class RegisterDeviceTokenRequest {

    @NotBlank
    private String token;

    @NotBlank
    private String platform;
}
