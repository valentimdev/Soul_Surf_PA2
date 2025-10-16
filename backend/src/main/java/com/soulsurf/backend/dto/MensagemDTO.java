package com.soulsurf.backend.dto;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class MensagemDTO {
    private Long id;
    private String texto;
    private LocalDateTime data;
    private UserDTO autor; // Retorna informações básicas do autor
    private Long praiaId;
}