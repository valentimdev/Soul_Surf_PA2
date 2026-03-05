package com.soulsurf.backend.modules.post.controller;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CreatePostRequest {
    private String descricao;
    private boolean publico;
    private Long beachId;
}

