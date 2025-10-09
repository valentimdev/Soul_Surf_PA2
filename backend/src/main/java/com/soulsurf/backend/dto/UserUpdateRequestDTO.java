package com.soulsurf.backend.dto;

import lombok.Data;

@Data
public class UserUpdateRequestDTO {

    private String username;

    private String fotoPerfil;

    private String fotoCapa;

    private String bio;

}