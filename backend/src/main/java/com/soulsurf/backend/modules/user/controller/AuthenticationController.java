package com.soulsurf.backend.modules.user.controller;

import com.soulsurf.backend.core.security.jwt.JwtUtils;
import com.soulsurf.backend.core.security.ratelimit.AuthRateLimitService;
import com.soulsurf.backend.core.security.service.UserDetailsImpl;
import com.soulsurf.backend.modules.chat.dto.MessageResponse;
import com.soulsurf.backend.modules.user.dto.UserDTO;
import com.soulsurf.backend.modules.user.service.PasswordResetService;
import com.soulsurf.backend.modules.user.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Value;
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

@RestController
@RequestMapping("/api/auth")
@Tag(name = "1. Autenticacao", description = "Endpoints para registro, login e recuperacao de senha.")
public class AuthenticationController {

    private final AuthenticationManager authenticationManager;
    private final JwtUtils jwtUtils;
    private final UserService userService;
    private final PasswordResetService passwordResetService;
    private final AuthRateLimitService authRateLimitService;

    @Value("${app.security.auth.max-attempts:6}")
    private int maxAttempts;

    @Value("${app.security.auth.window-seconds:900}")
    private int windowSeconds;

    @Value("${app.security.auth.block-seconds:900}")
    private int blockSeconds;

    public AuthenticationController(
            AuthenticationManager authenticationManager,
            JwtUtils jwtUtils,
            UserService userService,
            PasswordResetService passwordResetService,
            AuthRateLimitService authRateLimitService) {
        this.authenticationManager = authenticationManager;
        this.jwtUtils = jwtUtils;
        this.userService = userService;
        this.passwordResetService = passwordResetService;
        this.authRateLimitService = authRateLimitService;
    }

    @Operation(summary = "Autentica um usuario", description = "Realiza login com e-mail e senha e retorna um token JWT.")
    @ApiResponse(responseCode = "200", description = "Login bem-sucedido")
    @ApiResponse(responseCode = "401", description = "Credenciais invalidas")
    @ApiResponse(responseCode = "429", description = "Muitas tentativas")
    @PostMapping("/login")
    public ResponseEntity<?> authenticateUser(
            @Valid @RequestBody LoginRequest loginRequest,
            HttpServletRequest request) {

        String loginRateKey = buildRateKey("login", loginRequest.getEmail(), request);
        if (authRateLimitService.isBlocked(loginRateKey, windowSeconds)) {
            return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS)
                    .body(new MessageResponse("Muitas tentativas. Tente novamente em alguns minutos."));
        }

        try {
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(loginRequest.getEmail(), loginRequest.getPassword()));

            SecurityContextHolder.getContext().setAuthentication(authentication);
            String jwt = jwtUtils.generateJwtToken(authentication);
            authRateLimitService.recordSuccess(loginRateKey);

            boolean isAdmin = false;
            Object principal = authentication.getPrincipal();
            if (principal instanceof UserDetailsImpl userDetails) {
                isAdmin = userDetails.isAdmin();
            }

            return ResponseEntity.ok(new JwtResponse(jwt, isAdmin));
        } catch (Exception e) {
            authRateLimitService.recordFailure(loginRateKey, maxAttempts, windowSeconds, blockSeconds);
            return ResponseEntity
                    .status(HttpStatus.UNAUTHORIZED)
                    .body(new MessageResponse("Credenciais invalidas. Verifique seu email e senha."));
        }
    }

    @Operation(summary = "Registra novo usuario", description = "Cria uma nova conta de usuario.")
    @ApiResponse(responseCode = "201", description = "Usuario registrado com sucesso")
    @ApiResponse(responseCode = "400", description = "Email ou username ja em uso")
    @PostMapping("/signup")
    public ResponseEntity<?> registerUser(@Valid @RequestBody SignupRequest signUpRequest) {
        if (userService.existsByEmail(signUpRequest.getEmail())) {
            return ResponseEntity.badRequest().body(new MessageResponse("Erro: O e-mail ja esta em uso!"));
        }

        if (userService.existsByUsername(signUpRequest.getUsername())) {
            return ResponseEntity.badRequest().body(new MessageResponse("Erro: O username ja esta em uso!"));
        }

        UserDTO newUser = userService.registerUser(signUpRequest);
        return ResponseEntity.status(HttpStatus.CREATED).body(newUser);
    }

    @Operation(summary = "Solicita redefinicao de senha", description = "Envia um link de redefinicao para o e-mail informado.")
    @ApiResponse(responseCode = "200", description = "Solicitacao recebida")
    @ApiResponse(responseCode = "429", description = "Muitas tentativas")
    @PostMapping("/forgot-password")
    public ResponseEntity<MessageResponse> forgotPassword(
            @Valid @RequestBody ForgotPasswordRequest request,
            HttpServletRequest httpRequest) {

        String forgotRateKey = buildRateKey("forgot", request.getEmail(), httpRequest);
        if (authRateLimitService.isBlocked(forgotRateKey, windowSeconds)) {
            return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS)
                    .body(new MessageResponse("Muitas tentativas. Tente novamente em alguns minutos."));
        }

        passwordResetService.createPasswordResetToken(request.getEmail());
        // Conta a solicitacao para evitar flood de e-mails.
        authRateLimitService.recordFailure(forgotRateKey, maxAttempts, windowSeconds, blockSeconds);

        return ResponseEntity.ok(
                new MessageResponse("Se um e-mail valido for encontrado, um link de redefinicao sera enviado."));
    }

    @Operation(summary = "Redefine senha", description = "Atualiza a senha usando token de redefinicao.")
    @ApiResponse(responseCode = "200", description = "Senha atualizada com sucesso")
    @ApiResponse(responseCode = "400", description = "Token invalido ou expirado")
    @ApiResponse(responseCode = "429", description = "Muitas tentativas")
    @PostMapping("/reset-password")
    public ResponseEntity<MessageResponse> resetPassword(
            @Valid @RequestBody ResetPasswordRequest request,
            HttpServletRequest httpRequest) {

        String resetRateKey = buildRateKey("reset", null, httpRequest);
        if (authRateLimitService.isBlocked(resetRateKey, windowSeconds)) {
            return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS)
                    .body(new MessageResponse("Muitas tentativas. Tente novamente em alguns minutos."));
        }

        try {
            passwordResetService.resetPassword(request.getToken(), request.getNewPassword());
            authRateLimitService.recordSuccess(resetRateKey);
            return ResponseEntity.ok(new MessageResponse("Senha atualizada com sucesso!"));
        } catch (IllegalArgumentException e) {
            authRateLimitService.recordFailure(resetRateKey, maxAttempts, windowSeconds, blockSeconds);
            throw e;
        }
    }

    private String buildRateKey(String action, String email, HttpServletRequest request) {
        String ip = extractClientIp(request);
        String normalizedEmail = email == null ? "-" : email.trim().toLowerCase();
        return action + ":" + ip + ":" + normalizedEmail;
    }

    private String extractClientIp(HttpServletRequest request) {
        String forwarded = request.getHeader("X-Forwarded-For");
        if (forwarded != null && !forwarded.isBlank()) {
            return forwarded.split(",")[0].trim();
        }
        return request.getRemoteAddr();
    }
}
