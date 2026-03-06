package com.soulsurf.backend.modules.beach.dto;

import lombok.Data;
import java.time.LocalDateTime;
import com.soulsurf.backend.modules.user.dto.UserDTO;

@Data
public class BeachMessageDTO {
    private Long id;
    private String texto;
    private LocalDateTime data;
    private UserDTO autor; // Retorna informações básicas do autor
    private Long praiaId;
}

