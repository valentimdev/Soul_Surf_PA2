package com.soulsurf.backend.modules.user.mapper;

import com.soulsurf.backend.modules.user.dto.UserDTO;
import com.soulsurf.backend.modules.user.entity.User;
import org.springframework.stereotype.Component;

@Component
public class UserMapper {

    /**
     * Full DTO with follower/following counts (no posts).
     * Used for profile views, comment authors, notification senders, etc.
     */
    public UserDTO toDto(User user) {
        UserDTO dto = new UserDTO();
        dto.setId(user.getId());
        dto.setUsername(user.getUsername());
        dto.setEmail(user.getEmail());
        dto.setFotoPerfil(user.getFotoPerfil());
        dto.setFotoCapa(user.getFotoCapa());
        dto.setBio(user.getBio());
        dto.setAdmin(user.isAdmin());
        dto.setBanned(user.isBanned());

        if (user.getSeguidores() != null) {
            dto.setSeguidoresCount(user.getSeguidores().size());
        }
        if (user.getSeguindo() != null) {
            dto.setSeguindoCount(user.getSeguindo().size());
        }

        return dto;
    }

    /**
     * Minimal DTO for embedding inside other DTOs (post author, comment author).
     * Does NOT trigger lazy-loaded collections.
     */
    public UserDTO toSimpleDto(User user) {
        UserDTO dto = new UserDTO();
        dto.setId(user.getId());
        dto.setUsername(user.getUsername());
        dto.setEmail(user.getEmail());
        dto.setFotoPerfil(user.getFotoPerfil());
        dto.setFotoCapa(user.getFotoCapa());
        dto.setBio(user.getBio());
        dto.setAdmin(user.isAdmin());
        dto.setBanned(user.isBanned());
        return dto;
    }

    /**
     * Ultra-minimal DTO for autocomplete/mention suggestions.
     */
    public UserDTO toMentionDto(User user) {
        UserDTO dto = new UserDTO();
        dto.setId(user.getId());
        dto.setUsername(user.getUsername());
        dto.setFotoPerfil(user.getFotoPerfil());
        return dto;
    }
}

