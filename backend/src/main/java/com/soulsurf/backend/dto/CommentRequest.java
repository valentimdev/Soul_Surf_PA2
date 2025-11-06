package com.soulsurf.backend.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CommentRequest {
    @NotBlank(message = "O conteúdo do comentário não pode estar vazio")
    private String content;
    
    private String userEmail; // Opcional, pode ser obtido do token JWT
}

