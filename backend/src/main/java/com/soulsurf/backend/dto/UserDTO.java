package com.soulsurf.backend.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UserDTO {
    private Long id;
    private String email;

    public UserDTO(Long id, String email) {
        this.id = id;
        this.email = email;
    }
}