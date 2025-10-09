package com.soulsurf.backend.security.service;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.soulsurf.backend.entities.User;
import lombok.Getter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import java.util.Collection;

@Getter
public class UserDetailsImpl implements UserDetails {

    private final Long id;
    private final String email;
    @JsonIgnore
    private final String password;

    private final boolean admin;

    public UserDetailsImpl(Long id, String email, String password, boolean admin) {
        this.id = id;
        this.email = email;
        this.password = password;
        this.admin = admin;
    }

    public static UserDetailsImpl build(User user) {
        return new UserDetailsImpl(
                user.getId(),
                user.getEmail(),
                user.getPassword(),
                user.isAdmin());
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        if (admin) {
            return java.util.List.of(new SimpleGrantedAuthority("ROLE_ADMIN"));
        }
        return java.util.List.of(new SimpleGrantedAuthority("ROLE_USER"));
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
}