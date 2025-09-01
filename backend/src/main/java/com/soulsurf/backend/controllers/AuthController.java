// src/main/java/com/soulsurf/backend/controllers/AuthController.java

package com.soulsurf.backend.controllers;

import com.soulsurf.backend.dto.LoginRequest;
import com.soulsurf.backend.dto.JwtResponse;
import com.soulsurf.backend.security.jwt.JwtUtils; // Assuma que você já tem esta classe
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import jakarta.validation.Valid;

@CrossOrigin(origins = "*", maxAge = 3600)
@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthenticationManager authenticationManager;
    private final JwtUtils jwtUtils;

    // Construtor com injeção de dependências
    public AuthController(AuthenticationManager authenticationManager, JwtUtils jwtUtils) {
        this.authenticationManager = authenticationManager;
        this.jwtUtils = jwtUtils;
    }

    @PostMapping("/login")
    public ResponseEntity<?> authenticateUser(@Valid @RequestBody LoginRequest loginRequest) {
        try {
            // Tenta autenticar o usuário com o email e a senha fornecidos
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(loginRequest.getEmail(), loginRequest.getSenha())
            );

            // Armazena a autenticação no contexto de segurança
            SecurityContextHolder.getContext().setAuthentication(authentication);

            // Gera o token JWT para o usuário autenticado
            String jwt = jwtUtils.generateJwtToken(authentication);

            // Retorna o token em uma resposta de sucesso
            return ResponseEntity.ok(new JwtResponse(jwt));

        } catch (Exception e) {
            // Em caso de falha na autenticação (senha incorreta, usuário não encontrado, etc.)
            // Retorna uma resposta de erro 401 Unauthorized
            return ResponseEntity
                    .status(HttpStatus.UNAUTHORIZED)
                    .body("Credenciais inválidas. Por favor, verifique seu email e senha.");
        }
    }
}