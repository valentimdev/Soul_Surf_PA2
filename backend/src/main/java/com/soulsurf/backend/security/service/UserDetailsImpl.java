package com.soulsurf.backend.security.service;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.soulsurf.backend.entities.User;
import lombok.Getter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.Collections;
import java.util.UUID; // Importação adicionada para UUID

@Getter
public class UserDetailsImpl implements UserDetails {

    private final UUID id; // Tipo alterado para UUID
    private final String email;
    @JsonIgnore
    private final String password;

    // Construtor atualizado para receber um UUID
    public UserDetailsImpl(UUID id, String email, String password) {
        this.id = id;
        this.email = email;
        this.password = password;
    }

    public static UserDetailsImpl build(User user) {
        return new UserDetailsImpl(
                user.getId(), // Agora retorna um UUID
                user.getEmail(),
                user.getPassword());
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return Collections.emptyList();
    }

    @Override
    public String getPassword() {
        return password;
    }

    @Override
    public String getUsername() {
        return email;
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return true;
    }

    // O Lombok gera o getter para o id, agora do tipo UUID
    // public UUID getId() { return id; }
}