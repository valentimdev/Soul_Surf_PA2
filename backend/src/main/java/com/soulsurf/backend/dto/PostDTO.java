package com.soulsurf.backend.dto;

import lombok.Getter;
import lombok.Setter;
import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
public class PostDTO {
    private Long id;
    // private String titulo;
    private String descricao;
    private String caminhoFoto;
    private LocalDateTime data;
    private UserDTO usuario;
    private boolean publico;
    private BeachDTO beach;
    private List<CommentDTO> comments;
    private long likesCount;
    private boolean likedByCurrentUser;
}