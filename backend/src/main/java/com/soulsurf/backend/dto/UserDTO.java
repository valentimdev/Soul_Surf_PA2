package com.soulsurf.backend.dto;
import java.util.List;

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
    public String bio;
    private int seguidoresCount;
    private int seguindoCount;
    private List<PostDTO> posts;
    private boolean admin;
    private boolean banned;

    public UserDTO() {
    }
    public UserDTO(Long id, String email) {
        this.id = id;
        this.email = email;
    }
    public String getUsername() {
        return (username == null || username.isBlank()) ? "Surfista" : username;
    }

}