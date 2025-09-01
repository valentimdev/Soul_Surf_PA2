// src/main/java/com/soulsurf/backend/security/jwt/JwtUtils.java

package com.soulsurf.backend.security.jwt;

import io.jsonwebtoken.*;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

import java.security.Key;
import java.util.Date;

@Component
public class JwtUtils {

    private static final Logger logger = LoggerFactory.getLogger(JwtUtils.class);

    // O valor da chave secreta, obtido do arquivo application.properties
    @Value("${soulsurf.app.jwtSecret}")
    private String jwtSecret;

    // O tempo de expiração do token em milissegundos
    @Value("${soulsurf.app.jwtExpirationMs}")
    private int jwtExpirationMs;

    // 1. Método para gerar o token JWT
    public String generateJwtToken(Authentication authentication) {
        // Pega o nome de usuário (geralmente o email) do objeto de autenticação
        String username = authentication.getName();

        return Jwts.builder()
                .setSubject(username) // Define o assunto (quem é o dono do token)
                .setIssuedAt(new Date()) // Define a data de emissão
                .setExpiration(new Date((new Date()).getTime() + jwtExpirationMs)) // Define a data de expiração
                .signWith(key(), SignatureAlgorithm.HS256) // Assina o token com a chave secreta
                .compact(); // Constrói o token e o comprime em uma string
    }

    // 2. Método para gerar a chave de assinatura
    private Key key() {
        return Keys.hmacShaKeyFor(Decoders.BASE64.decode(jwtSecret));
    }

    // 3. Método para extrair o nome de usuário do token
    public String getUserNameFromJwtToken(String token) {
        return Jwts.parserBuilder().setSigningKey(key()).build()
                .parseClaimsJws(token).getBody().getSubject();
    }

    // 4. Método para validar o token JWT
    public boolean validateJwtToken(String authToken) {
        try {
            Jwts.parserBuilder().setSigningKey(key()).build().parse(authToken);
            return true;
        } catch (MalformedJwtException e) {
            logger.error("Token JWT inválido: {}", e.getMessage());
        } catch (ExpiredJwtException e) {
            logger.error("Token JWT expirado: {}", e.getMessage());
        } catch (UnsupportedJwtException e) {
            logger.error("Token JWT não suportado: {}", e.getMessage());
        } catch (IllegalArgumentException e) {
            logger.error("A string de claims do JWT está vazia: {}", e.getMessage());
        }
        return false;
    }
}