package com.soulsurf.backend.dto;// src/main/java/com/soulsurf/backend/payload/response/JwtResponse.java

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class JwtResponse {
    private String token;
    private String type = "Bearer";
    private boolean admin;
    public JwtResponse(String accessToken) {
        this.token = accessToken;
    }
    public JwtResponse(String accessToken, boolean admin) {
        this.token = accessToken;
        this.admin = admin;
    }
}