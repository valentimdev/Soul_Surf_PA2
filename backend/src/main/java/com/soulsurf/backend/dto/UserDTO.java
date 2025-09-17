package com.soulsurf.backend.dto;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UserDTO {
    private Long id;
    private String username;
    private String email;
    private String fotoPerfil;
    private String fotoCapa;
    private int seguidoresCount;
    private int seguindoCount;
    private List<PostDTO> posts;

    public UserDTO() {
    }
    public UserDTO(Long id, String email) {
    this.id = id;
    this.email = email;
    }
}