package com.soulsurf.backend.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
public class CommentDTO {
    private Long id;
    private String texto;
    private LocalDateTime data;
    private UserDTO usuario;
}