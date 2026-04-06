package com.soulsurf.backend.modules.user.service;

import com.soulsurf.backend.modules.user.entity.PasswordResetToken;
import com.soulsurf.backend.modules.user.entity.User;
import com.soulsurf.backend.modules.user.repository.PasswordResetTokenRepository;
import com.soulsurf.backend.modules.user.repository.UserRepository;
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

    private static final long EXPIRATION_TIME_MILLIS = 1000L * 60L * 60L * 24L; // 24h
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
        Optional<User> userOptional = userRepository.findByEmail(email);

        if (userOptional.isPresent()) {
            User user = userOptional.get();
            String rawToken = generateSecureToken();
            String tokenHash = hashToken(rawToken);
            Instant expiryDate = Instant.now().plus(EXPIRATION_TIME_MILLIS, ChronoUnit.MILLIS);

            tokenRepository.findByUser(user).ifPresent(tokenRepository::delete);

            PasswordResetToken passwordResetToken = new PasswordResetToken(tokenHash, user, expiryDate);
            tokenRepository.save(passwordResetToken);

            emailService.sendPasswordResetEmail(user.getEmail(), rawToken);
        }
    }

    public void resetPassword(String token, String newPassword) {
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
}
