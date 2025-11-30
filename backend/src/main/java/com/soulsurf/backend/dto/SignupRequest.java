package com.soulsurf.backend.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class SignupRequest {
    @NotBlank
    @Email
    private String email;

    @NotBlank
    @Size(min = 6, max = 40)
    private String password;

    @NotBlank
    @Size(min = 6, max = 40)
    @Pattern(regexp = "^[a-zA-Z0-9_-]+$", message = "O username deve conter apenas letras, números, underscore (_) ou hífen (-). Não são permitidos espaços ou acentos.")
    private String username;
}