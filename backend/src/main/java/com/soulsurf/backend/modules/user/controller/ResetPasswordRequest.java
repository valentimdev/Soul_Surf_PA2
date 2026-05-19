// src/main/java/com/soulsurf/backend/dto/ResetPasswordRequest.java

package com.soulsurf.backend.modules.user.controller;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ResetPasswordRequest {

    @Email
    private String email;

    @Pattern(regexp = "^\\d{6}$", message = "O codigo deve conter 6 digitos numericos.")
    private String code;

    private String token;

    @NotBlank
    @Size(min = 6, max = 40, message = "A senha deve ter entre 6 e 40 caracteres.")
    private String newPassword;
}

