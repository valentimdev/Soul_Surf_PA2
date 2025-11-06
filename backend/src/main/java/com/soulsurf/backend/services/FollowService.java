package com.soulsurf.backend.services;

import com.soulsurf.backend.dto.UserDTO;
import com.soulsurf.backend.entities.User;
import com.soulsurf.backend.repository.UserRepository;
import com.soulsurf.backend.security.AuthUtils;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.transaction.annotation.Transactional;

@Service
public class FollowService {
    private final UserRepository userRepo;
    public FollowService(UserRepository userRepo){ this.userRepo = userRepo; }

    @Transactional(readOnly = true)
    public List<UserDTO> listMyFollowing() {
        String meEmail = AuthUtils.currentUserId(); // precisa retornar o EMAIL
        var me = userRepo.findByEmail(meEmail)
                .orElseThrow(() -> new RuntimeException("Usuário não encontrado"));

        return me.getSeguindo().stream().map(u -> {
            var dto = new UserDTO();
            dto.setId(u.getId());
            dto.setEmail(u.getEmail());
            dto.setUsername(u.getUsername());
            dto.setFotoPerfil(u.getFotoPerfil());
            dto.setFotoCapa(u.getFotoCapa());
            dto.setBio(u.getBio());
            return dto;
        }).toList();
    }
}

