package com.soulsurf.backend.modules.user.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.soulsurf.backend.modules.post.dto.PostDTO;
import java.util.List;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UserDTO {
    private Long id;
    private String username;
    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    private String email;
    private String fotoPerfil;
    private String fotoCapa;
    public String bio;
    private int seguidoresCount;
    private int seguindoCount;
    private List<PostDTO> posts;
    private boolean admin;
    private boolean banned;
    private boolean isFollowing;

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

