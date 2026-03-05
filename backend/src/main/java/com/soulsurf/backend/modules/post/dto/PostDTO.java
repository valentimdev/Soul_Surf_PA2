package com.soulsurf.backend.modules.post.dto;

import lombok.Getter;
import lombok.Setter;
import java.time.LocalDateTime;
import java.util.List;

import com.soulsurf.backend.modules.user.dto.UserDTO;
import com.soulsurf.backend.modules.beach.dto.BeachDTO;
import com.soulsurf.backend.modules.comment.dto.CommentDTO;

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
    private long commentsCount;
    private boolean likedByCurrentUser;
}

