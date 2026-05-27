package com.soulsurf.backend.modules.user.service;

import com.soulsurf.backend.modules.user.controller.ResetPasswordRequest;
import com.soulsurf.backend.modules.user.entity.PasswordResetToken;
import com.soulsurf.backend.modules.user.entity.User;
import com.soulsurf.backend.modules.user.repository.PasswordResetTokenRepository;
import com.soulsurf.backend.modules.user.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Base64;
import java.util.HexFormat;
import java.util.Optional;

@Service
public class PasswordResetService {

    private static final Logger log = LoggerFactory.getLogger(PasswordResetService.class);
    private static final long EXPIRATION_TIME_MILLIS = 1000L * 60L * 15L; // 15 min
    private static final SecureRandom SECURE_RANDOM = new SecureRandom();

    private final UserRepository userRepository;
    private final PasswordResetTokenRepository tokenRepository;
    private final EmailService emailService;
    private final PasswordEncoder passwordEncoder;

    public PasswordResetService(
            UserRepository userRepository,
            PasswordResetTokenRepository tokenRepository,
            EmailService emailService,
            PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.tokenRepository = tokenRepository;
        this.emailService = emailService;
        this.passwordEncoder = passwordEncoder;
    }

    public void createPasswordResetToken(String email) {
        Optional<User> userOptional = findUserByEmail(email);

        if (userOptional.isPresent()) {
            User user = userOptional.get();
            String rawToken = generateSecureToken();
            String tokenHash = hashToken(rawToken);
            String resetCode = deriveResetCode(tokenHash, user.getEmail());
            Instant expiryDate = Instant.now().plus(EXPIRATION_TIME_MILLIS, ChronoUnit.MILLIS);

            tokenRepository.findByUser(user).ifPresent(tokenRepository::delete);

            PasswordResetToken passwordResetToken = new PasswordResetToken(tokenHash, user, expiryDate);
            tokenRepository.save(passwordResetToken);

            try {
                emailService.sendPasswordResetEmail(user.getEmail(), rawToken, resetCode);
            } catch (RuntimeException emailEx) {
                log.error("Falha ao enviar email de redefinicao de senha para {}: {}",
                        user.getEmail(), emailEx.getMessage());
                // Token salvo — nao propaga o erro para o usuario
            }
        }
    }

    public void resetPassword(ResetPasswordRequest request) {
        if (hasText(request.getToken())) {
            resetPasswordByToken(request.getToken(), request.getNewPassword());
            return;
        }

        if (hasText(request.getEmail()) && hasText(request.getCode())) {
            resetPasswordByCode(request.getEmail(), request.getCode(), request.getNewPassword());
            return;
        }

        throw new IllegalArgumentException("Informe token ou email + codigo para redefinir a senha.");
    }

    private void resetPasswordByToken(String token, String newPassword) {
        String tokenHash = hashToken(token);
        Optional<PasswordResetToken> tokenOptional = tokenRepository.findByToken(tokenHash);

        if (tokenOptional.isEmpty()) {
            throw new IllegalArgumentException("Token de redefinicao invalido.");
        }

        PasswordResetToken passwordResetToken = tokenOptional.get();
        if (passwordResetToken.getExpiryDate().isBefore(Instant.now())) {
            tokenRepository.delete(passwordResetToken);
            throw new IllegalArgumentException("Token de redefinicao expirado.");
        }

        User user = passwordResetToken.getUser();
        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);

        tokenRepository.delete(passwordResetToken);
    }

    private void resetPasswordByCode(String email, String code, String newPassword) {
        Optional<User> userOptional = findUserByEmail(email);
        if (userOptional.isEmpty()) {
            throw new IllegalArgumentException("Codigo invalido ou expirado.");
        }

        User user = userOptional.get();
        Optional<PasswordResetToken> tokenOptional = tokenRepository.findByUser(user);
        if (tokenOptional.isEmpty()) {
            throw new IllegalArgumentException("Codigo invalido ou expirado.");
        }

        PasswordResetToken passwordResetToken = tokenOptional.get();
        if (passwordResetToken.getExpiryDate().isBefore(Instant.now())) {
            tokenRepository.delete(passwordResetToken);
            throw new IllegalArgumentException("Codigo invalido ou expirado.");
        }

        String expectedCode = deriveResetCode(passwordResetToken.getToken(), user.getEmail());
        if (!expectedCode.equals(code)) {
            throw new IllegalArgumentException("Codigo invalido ou expirado.");
        }

        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);
        tokenRepository.delete(passwordResetToken);
    }

    private String generateSecureToken() {
        byte[] bytes = new byte[32];
        SECURE_RANDOM.nextBytes(bytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }

    private String hashToken(String token) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(token.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(hash);
        } catch (Exception e) {
            throw new IllegalStateException("Nao foi possivel processar token de redefinicao.", e);
        }
    }

    private String deriveResetCode(String tokenHash, String email) {
        String derivation = hashToken(tokenHash + ":" + email.trim().toLowerCase());
        long numeric = Long.parseUnsignedLong(derivation.substring(0, 12), 16);
        return String.format("%06d", numeric % 1_000_000);
    }

    private boolean hasText(String value) {
        return value != null && !value.trim().isEmpty();
    }

    private Optional<User> findUserByEmail(String email) {
        if (!hasText(email)) {
            return Optional.empty();
        }

        String trimmed = email.trim();
        String normalized = trimmed.toLowerCase();
        Optional<User> normalizedMatch = userRepository.findByEmail(normalized);
        if (normalizedMatch.isPresent()) {
            return normalizedMatch;
        }

        if (!trimmed.equals(normalized)) {
            return userRepository.findByEmail(trimmed);
        }

        return Optional.empty();
    }
}
