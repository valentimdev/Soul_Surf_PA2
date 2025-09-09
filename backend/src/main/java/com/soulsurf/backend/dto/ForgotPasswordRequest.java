// src/main/java/com/soulsurf/backend/payload/request/ForgotPasswordRequest.java

package com.soulsurf.backend.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ForgotPasswordRequest {

    @NotBlank
    @Email
    private String email;
}