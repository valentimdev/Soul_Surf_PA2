package com.soulsurf.backend.security;

import org.springframework.security.core.context.SecurityContextHolder;

public class AuthUtils {
    public static String currentUserId() {
        var auth = SecurityContextHolder.getContext().getAuthentication();
        return auth != null ? auth.getName() : null;
    }
}
