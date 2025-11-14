// src/main/java/com/soulsurf/backend/security/jwt/AuthTokenFilter.java

package com.soulsurf.backend.security.jwt;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import com.soulsurf.backend.security.service.UserDetailsServiceImpl;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static com.fasterxml.jackson.databind.cfg.CoercionInputShape.Boolean;

@Component
public class AuthTokenFilter extends OncePerRequestFilter {

    @Autowired
    private JwtUtils jwtUtils;

    @Autowired
    private UserDetailsServiceImpl userDetailsService;

    private static final Logger logger = LoggerFactory.getLogger(AuthTokenFilter.class);

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        logger.info("=== DEBUG: Processando requisição: {} {}", request.getMethod(), request.getRequestURI());
        try {
            // Extrai o token do cabeçalho da requisição
            String jwt = parseJwt(request);
            logger.info("=== DEBUG: Token extraído: {}", jwt != null ? "Token presente" : "Token ausente");
            if (jwt != null && jwtUtils.validateJwtToken(jwt)) {
                // Se o token for válido, extrai o nome de usuário (email)
                logger.info("=== DEBUG: Token válido!");
                String username = jwtUtils.getUserNameFromJwtToken(jwt);
                logger.info("=== DEBUG: Username extraído do token: {}", username);

                // Carrega os detalhes do usuário usando o service que você criou
                UserDetails userDetails = userDetailsService.loadUserByUsername(username);

                // Lê o JWT e extrai os claims
                Claims claims = Jwts.parserBuilder()
                        .setSigningKey(jwtUtils.getKey()) // PRECISAMOS expor getKey() no JwtUtils
                        .build()
                        .parseClaimsJws(jwt)
                        .getBody();

                // Copia as roles normais do usuário
                List<GrantedAuthority> authorities = new ArrayList<>(userDetails.getAuthorities());

                // Se o token tiver admin=true, adiciona ROLE_ADMIN
                Boolean isAdmin = claims.get("admin", Boolean.class);
                if (isAdmin != null && isAdmin) {
                    authorities.add(new SimpleGrantedAuthority("ROLE_ADMIN"));
                }
                logger.info("=== AUTHORITIES DO USUÁRIO: {}", userDetails.getAuthorities());
                UsernamePasswordAuthenticationToken authentication =
                        new UsernamePasswordAuthenticationToken(
                                userDetails,
                                null,
                                authorities
                        );

                authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));

                // Define o objeto de autenticação no contexto de segurança do Spring
                SecurityContextHolder.getContext().setAuthentication(authentication);
            }
        } catch (Exception e) {
            logger.error("Não foi possível definir a autenticação do usuário: {}", e.getMessage());
        }

        // Continua a cadeia de filtros
        filterChain.doFilter(request, response);
    }

    private String parseJwt(HttpServletRequest request) {
        String headerAuth = request.getHeader("Authorization");

        if (StringUtils.hasText(headerAuth) && headerAuth.startsWith("Bearer ")) {
            // Retorna apenas o token, sem o prefixo "Bearer "
            return headerAuth.substring(7);
        }

        return null;
    }
}