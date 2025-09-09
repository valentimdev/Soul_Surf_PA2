package com.soulsurf.backend.services;

import com.soulsurf.backend.entities.PasswordResetToken;
import com.soulsurf.backend.entities.User;
import com.soulsurf.backend.repository.PasswordResetTokenRepository;
import com.soulsurf.backend.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.Optional;
import java.util.UUID;

@Service
public class PasswordResetService {

    private static final long EXPIRATION_TIME = 1000 * 60 * 60 * 24; // 24 horas em milissegundos

    private final UserRepository userRepository;
    private final PasswordResetTokenRepository tokenRepository;
    private final EmailService emailService;
    private final PasswordEncoder passwordEncoder; // Injeção do PasswordEncoder

    @Autowired
    public PasswordResetService(UserRepository userRepository, PasswordResetTokenRepository tokenRepository, EmailService emailService, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.tokenRepository = tokenRepository;
        this.emailService = emailService;
        this.passwordEncoder = passwordEncoder;
    }

    public void createPasswordResetToken(String email) {
        Optional<User> userOptional = userRepository.findByEmail(email);

        if (userOptional.isPresent()) {
            User user = userOptional.get();
            String token = UUID.randomUUID().toString();
            Date expiryDate = new Date(System.currentTimeMillis() + EXPIRATION_TIME);

            tokenRepository.findByUser(user).ifPresent(tokenRepository::delete);

            PasswordResetToken passwordResetToken = new PasswordResetToken(token, user, expiryDate);
            tokenRepository.save(passwordResetToken);

            emailService.sendPasswordResetEmail(user.getEmail(), token);
        }
    }

    public void resetPassword(String token, String newPassword) {
        // 1. Busca o token no banco de dados
        Optional<PasswordResetToken> tokenOptional = tokenRepository.findByToken(token);

        if (tokenOptional.isEmpty()) {
            throw new IllegalArgumentException("Token de redefinição inválido.");
        }

        PasswordResetToken passwordResetToken = tokenOptional.get();

        // 2. Valida se o token expirou
        if (passwordResetToken.getExpiryDate().before(new Date())) {
            // Se o token estiver expirado, ele é removido do banco de dados
            tokenRepository.delete(passwordResetToken);
            throw new IllegalArgumentException("Token de redefinição expirado.");
        }

        // 3. Atualiza a senha do usuário
        User user = passwordResetToken.getUser();
        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);

        // 4. Invalida o token
        tokenRepository.delete(passwordResetToken);
    }
}