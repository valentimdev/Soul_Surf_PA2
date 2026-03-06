package com.soulsurf.backend.modules.comment.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import java.time.LocalDateTime;

import com.soulsurf.backend.modules.user.dto.UserDTO;
import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
public class CommentDTO {
    private Long id;
    private String texto;
    private LocalDateTime data;
    private UserDTO usuario;
    private Long parentId;
    private List<CommentDTO> replies = new ArrayList<>();
}

