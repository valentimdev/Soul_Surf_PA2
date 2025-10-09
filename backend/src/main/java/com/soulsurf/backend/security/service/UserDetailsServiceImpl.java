// src/main/java/com/soulsurf/backend/security/services/UserDetailsServiceImpl.java

package com.soulsurf.backend.security.service;

import com.soulsurf.backend.entities.User;
import com.soulsurf.backend.repository.UserRepository; // Assuma que você tem seu repositório de usuários
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class UserDetailsServiceImpl implements UserDetailsService {

    private final UserRepository userRepository;

    public UserDetailsServiceImpl(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    @Transactional
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("Usuário não encontrado com o e-mail: " + email));
        return UserDetailsImpl.build(user);
    }
}