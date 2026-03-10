package com.soulsurf.backend.modules.user.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.soulsurf.backend.BaseIntegrationTest;
import com.soulsurf.backend.modules.user.entity.PasswordResetToken;
import com.soulsurf.backend.modules.user.entity.User;
import com.soulsurf.backend.modules.user.repository.PasswordResetTokenRepository;
import com.soulsurf.backend.modules.user.repository.UserRepository;
import com.soulsurf.backend.modules.user.service.EmailService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.UUID;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;

public class AuthenticationControllerTest extends BaseIntegrationTest {

        @Autowired
        private ObjectMapper objectMapper;

        @Autowired
        private UserRepository userRepository;

        @Autowired
        private PasswordResetTokenRepository tokenRepository;

        @MockBean
        private EmailService emailService;

        @Test
        public void testSignupAndLogin() throws Exception {
                SignupRequest signupRequest = new SignupRequest();
                signupRequest.setEmail("test@example.com");
                signupRequest.setPassword("password123");
                signupRequest.setUsername("testuser");

                // 1. Signup
                mockMvc.perform(post("/api/auth/signup")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(signupRequest)))
                                .andExpect(status().isCreated())
                                .andExpect(jsonPath("$.email").value("test@example.com"))
                                .andExpect(jsonPath("$.username").value("testuser"));

                // 2. Login
                LoginRequest loginRequest = new LoginRequest();
                loginRequest.setEmail("test@example.com");
                loginRequest.setPassword("password123");

                mockMvc.perform(post("/api/auth/login")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(loginRequest)))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.token").exists());
        }

        @Test
        public void testLoginFailure() throws Exception {
                LoginRequest loginRequest = new LoginRequest();
                loginRequest.setEmail("nonexistent@example.com");
                loginRequest.setPassword("wrongpassword");

                mockMvc.perform(post("/api/auth/login")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(loginRequest)))
                                .andExpect(status().isUnauthorized());
        }

        @Test
        public void testForgotPassword() throws Exception {
                // Create a user first
                SignupRequest signup = new SignupRequest();
                signup.setEmail("forgot@example.com");
                signup.setPassword("password123");
                signup.setUsername("forgotuser");

                mockMvc.perform(post("/api/auth/signup")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(signup)))
                                .andExpect(status().isCreated());

                // Request password reset
                ForgotPasswordRequest forgotRequest = new ForgotPasswordRequest();
                forgotRequest.setEmail("forgot@example.com");

                mockMvc.perform(post("/api/auth/forgot-password")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(forgotRequest)))
                                .andExpect(status().isOk());
        }

        @Test
        public void testResetPassword() throws Exception {
                // Create a user
                SignupRequest signup = new SignupRequest();
                signup.setEmail("reset@example.com");
                signup.setPassword("password123");
                signup.setUsername("resetuser");

                mockMvc.perform(post("/api/auth/signup")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(signup)))
                                .andExpect(status().isCreated());

                // Manually create a reset token in DB
                User user = userRepository.findByEmail("reset@example.com").orElseThrow();
                String token = UUID.randomUUID().toString();
                Instant expiryDate = Instant.now().plus(24, ChronoUnit.HOURS);
                PasswordResetToken resetToken = new PasswordResetToken(token, user, expiryDate);
                tokenRepository.save(resetToken);

                // Use the token to reset password
                ResetPasswordRequest resetRequest = new ResetPasswordRequest();
                resetRequest.setToken(token);
                resetRequest.setNewPassword("newpassword123");

                mockMvc.perform(post("/api/auth/reset-password")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(resetRequest)))
                                .andExpect(status().isOk());

                // Verify new password works
                LoginRequest login = new LoginRequest();
                login.setEmail("reset@example.com");
                login.setPassword("newpassword123");

                mockMvc.perform(post("/api/auth/login")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(login)))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.token").exists());
        }
}
