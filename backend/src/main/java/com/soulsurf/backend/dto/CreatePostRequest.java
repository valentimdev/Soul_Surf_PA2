package com.soulsurf.backend.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CreatePostRequest {
    private String descricao;
    private boolean publico;
    private Long beachId;
}