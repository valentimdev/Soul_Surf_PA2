package com.soulsurf.backend.modules.user.service;

import com.soulsurf.backend.modules.user.dto.UserDTO;
import com.soulsurf.backend.modules.user.entity.User;
import com.soulsurf.backend.modules.user.repository.UserRepository;
import com.soulsurf.backend.core.security.AuthUtils;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.transaction.annotation.Transactional;

@Service
public class FollowService {
    private final UserRepository userRepo;

    public FollowService(UserRepository userRepo) {
        this.userRepo = userRepo;
    }

    @Transactional(readOnly = true)
    public List<UserDTO> listMyFollowing() {
        String meEmail = AuthUtils.currentUserId(); // precisa retornar o EMAIL
        var me = userRepo.findByEmail(meEmail)
                .orElseThrow(() -> new RuntimeException("Usuário não encontrado"));

        return me.getSeguindo().stream().map(u -> {
            var dto = new UserDTO();
            dto.setId(u.getId());
            dto.setUsername(u.getUsername());
            dto.setFotoPerfil(u.getFotoPerfil());
            dto.setFotoCapa(u.getFotoCapa());
            dto.setBio(u.getBio());
            return dto;
        }).toList();
    }
}

