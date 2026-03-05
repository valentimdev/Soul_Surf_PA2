package com.soulsurf.backend.modules.user.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.soulsurf.backend.modules.user.controller.ForgotPasswordRequest;
import com.soulsurf.backend.modules.user.controller.JwtResponse;
import com.soulsurf.backend.modules.user.controller.LoginRequest;
import com.soulsurf.backend.modules.chat.dto.MessageResponse;
import com.soulsurf.backend.modules.user.controller.ResetPasswordRequest;
import com.soulsurf.backend.modules.user.controller.SignupRequest;
import com.soulsurf.backend.modules.user.dto.UserDTO;
import com.soulsurf.backend.core.security.jwt.JwtUtils;
import com.soulsurf.backend.core.security.service.UserDetailsImpl;
import com.soulsurf.backend.modules.user.service.PasswordResetService;
import com.soulsurf.backend.modules.user.service.UserService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/auth")
@Tag(name = "1. Autenticação", description = "Endpoints para registro, login e recuperação de senha.")
public class AuthenticationController {

    private final AuthenticationManager authenticationManager;
    private final JwtUtils jwtUtils;
    private final UserService userService;
    private final PasswordResetService passwordResetService;

    public AuthenticationController(AuthenticationManager authenticationManager, JwtUtils jwtUtils,
            UserService userService,
            PasswordResetService passwordResetService) {
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
            boolean isAdmin = false;
            Object principal = authentication.getPrincipal();
            if (principal instanceof UserDetailsImpl) {
                isAdmin = ((UserDetailsImpl) principal).isAdmin();
            }
            return ResponseEntity.ok(new JwtResponse(jwt, isAdmin));

        } catch (Exception e) {
            return ResponseEntity
                    .status(HttpStatus.UNAUTHORIZED)
                    .body(new MessageResponse("Credenciais inválidas. Por favor, verifique seu email e senha."));
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

        if (userService.existsByUsername(signUpRequest.getUsername())) {
            return ResponseEntity
                    .badRequest()
                    .body(new MessageResponse("Erro: O username já está em uso!"));
        }

        UserDTO newUser = userService.registerUser(signUpRequest);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(newUser);
    }

    @Operation(summary = "Solicita redefinição de senha", description = "Envia um link de redefinição de senha para o e-mail do usuário.")
    @ApiResponse(responseCode = "200", description = "Se um e-mail válido for encontrado, um link de redefinição será enviado.")
    @PostMapping("/forgot-password")
    public ResponseEntity<MessageResponse> forgotPassword(@Valid @RequestBody ForgotPasswordRequest request) {
        passwordResetService.createPasswordResetToken(request.getEmail());
        return ResponseEntity
                .ok(new MessageResponse("Se um e-mail válido for encontrado, um link de redefinição será enviado."));
    }

    @Operation(summary = "Redefine a senha do usuário", description = "Atualiza a senha do usuário usando o token de redefinição.")
    @ApiResponse(responseCode = "200", description = "Senha atualizada com sucesso")
    @ApiResponse(responseCode = "400", description = "Token inválido ou expirado")
    @PostMapping("/reset-password")
    public ResponseEntity<MessageResponse> resetPassword(@Valid @RequestBody ResetPasswordRequest request) {
        passwordResetService.resetPassword(request.getToken(), request.getNewPassword());
        return ResponseEntity.ok(new MessageResponse("Senha atualizada com sucesso!"));
    }
}
