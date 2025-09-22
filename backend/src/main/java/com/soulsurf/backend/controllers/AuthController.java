package com.soulsurf.backend.controllers;

import com.soulsurf.backend.dto.*;
import com.soulsurf.backend.security.jwt.JwtUtils;
import com.soulsurf.backend.services.PasswordResetService;
import com.soulsurf.backend.services.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
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
@Tag(name = "1. Autenticação", description = "Endpoints para registro, login e recuperação de senha.")
public class AuthController {

    private final AuthenticationManager authenticationManager;
    private final JwtUtils jwtUtils;
    private final UserService userService;
    private final PasswordResetService passwordResetService;

    public AuthController(AuthenticationManager authenticationManager, JwtUtils jwtUtils, UserService userService, PasswordResetService passwordResetService) {
        this.authenticationManager = authenticationManager;
        this.jwtUtils = jwtUtils;
        this.userService = userService;
        this.passwordResetService = passwordResetService;
    }

    @Operation(summary = "Autentica um usuário", description = "Realiza o login do usuário com e-mail e senha e retorna um token JWT.")
    @ApiResponse(responseCode = "200", description = "Login bem-sucedido, retorna o token JWT")
    @ApiResponse(responseCode = "401", description = "Credenciais inválidas")
    @PostMapping("/login")
    public ResponseEntity<?> authenticateUser(@Valid @RequestBody LoginRequest loginRequest) {
        try {
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(loginRequest.getEmail(), loginRequest.getPassword()));

            SecurityContextHolder.getContext().setAuthentication(authentication);
            String jwt = jwtUtils.generateJwtToken(authentication);

            return ResponseEntity.ok(new JwtResponse(jwt));

        } catch (Exception e) {
            return ResponseEntity
                    .status(HttpStatus.UNAUTHORIZED)
                    .body("Credenciais inválidas. Por favor, verifique seu email e senha.");
        }
    }

    @Operation(summary = "Registra um novo usuário", description = "Cria uma nova conta de usuário.")
    @ApiResponse(responseCode = "201", description = "Usuário registrado com sucesso")
    @ApiResponse(responseCode = "400", description = "O e-mail já está em uso")
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

    @Operation(summary = "Solicita redefinição de senha", description = "Envia um link de redefinição de senha para o e-mail do usuário.")
    @ApiResponse(responseCode = "200", description = "Se um e-mail válido for encontrado, um link de redefinição será enviado.")
    @PostMapping("/forgot-password")
    public ResponseEntity<?> forgotPassword(@Valid @RequestBody ForgotPasswordRequest request) {
        passwordResetService.createPasswordResetToken(request.getEmail());
        return ResponseEntity.ok(new MessageResponse("Se um e-mail válido for encontrado, um link de redefinição será enviado."));
    }

    @Operation(summary = "Redefine a senha do usuário", description = "Atualiza a senha do usuário usando o token de redefinição.")
    @ApiResponse(responseCode = "200", description = "Senha atualizada com sucesso")
    @ApiResponse(responseCode = "400", description = "Token inválido ou expirado")
    @PostMapping("/reset-password")
    public ResponseEntity<?> resetPassword(@Valid @RequestBody ResetPasswordRequest request) {
        try {
            passwordResetService.resetPassword(request.getToken(), request.getNewPassword());
            return ResponseEntity.ok(new MessageResponse("Senha atualizada com sucesso!"));
        } catch (Exception e) {
            return ResponseEntity
                    .badRequest()
                    .body(new MessageResponse(e.getMessage()));
        }
    }
}