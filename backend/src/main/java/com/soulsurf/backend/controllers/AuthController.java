package com.soulsurf.backend.controllers;

import com.soulsurf.backend.dto.JwtResponse;
import com.soulsurf.backend.dto.LoginRequest;
import com.soulsurf.backend.dto.MessageResponse;
import com.soulsurf.backend.dto.SignupRequest;
import com.soulsurf.backend.security.jwt.JwtUtils;
import com.soulsurf.backend.services.PasswordResetService; // Importação adicionada
import com.soulsurf.backend.services.UserService;
import com.soulsurf.backend.dto.ForgotPasswordRequest; // Importação adicionada
import com.soulsurf.backend.dto.ResetPasswordRequest; // Importação adicionada
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

@CrossOrigin(origins = "*", maxAge = 3600)
@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthenticationManager authenticationManager;
    private final JwtUtils jwtUtils;
    private final UserService userService;
    private final PasswordResetService passwordResetService; // Injeção do novo serviço

    public AuthController(AuthenticationManager authenticationManager, JwtUtils jwtUtils, UserService userService, PasswordResetService passwordResetService) {
        this.authenticationManager = authenticationManager;
        this.jwtUtils = jwtUtils;
        this.userService = userService;
        this.passwordResetService = passwordResetService;
    }

    @PostMapping("/login")
    public ResponseEntity<?> authenticateUser(@Valid @RequestBody LoginRequest loginRequest) {
        try {
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(loginRequest.getEmail(), loginRequest.getPassword()));

            SecurityContextHolder.getContext().setAuthentication(authentication);
            System.out.println("AUTENTICADO: " + authentication.getName());
            String jwt = jwtUtils.generateJwtToken(authentication);

            return ResponseEntity.ok(new JwtResponse(jwt));

        } catch (Exception e) {
            return ResponseEntity
                    .status(HttpStatus.UNAUTHORIZED)
                    .body("Credenciais inválidas. Por favor, verifique seu email e senha.");
        }
    }

    @PostMapping("/signup")
    public ResponseEntity<?> registerUser(@Valid @RequestBody SignupRequest signUpRequest) {
        if (userService.existsByEmail(signUpRequest.getEmail())) {
            return ResponseEntity
                    .badRequest()
                    .body(new MessageResponse("Erro: O e-mail já está em uso!"));
        }

        userService.registerUser(signUpRequest);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(new MessageResponse("Usuário registrado com sucesso!"));
    }

        @PostMapping("/forgot-password")
        public ResponseEntity<?> forgotPassword(@Valid @RequestBody ForgotPasswordRequest request) {
            passwordResetService.createPasswordResetToken(request.getEmail());

            // Mensagem genérica por segurança, mesmo se o e-mail não existir
            return ResponseEntity.ok(new MessageResponse("Se um e-mail válido for encontrado, um link de redefinição será enviado."));
        }

        @PostMapping("/reset-password")
        public ResponseEntity<?> resetPassword(@Valid @RequestBody ResetPasswordRequest request) {
            try {
                passwordResetService.resetPassword(request.getToken(), request.getNewPassword());
                return ResponseEntity.ok(new MessageResponse("Senha atualizada com sucesso!"));
            } catch (Exception e) {
                // Captura erros de token inválido, expirado, etc.
                return ResponseEntity
                        .badRequest()
                        .body(new MessageResponse(e.getMessage()));
            }
        }
}