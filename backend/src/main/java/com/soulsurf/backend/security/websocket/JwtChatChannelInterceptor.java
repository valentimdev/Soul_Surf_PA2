// src/main/java/com/soulsurf/backend/security/websocket/JwtChatChannelInterceptor.java
package com.soulsurf.backend.security.websocket;

import com.soulsurf.backend.security.jwt.JwtUtils;
import com.soulsurf.backend.security.service.UserDetailsServiceImpl;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessagingException;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextImpl;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class JwtChatChannelInterceptor implements ChannelInterceptor {

    private final JwtUtils jwtUtils;
    private final UserDetailsServiceImpl userDetailsService;

    public JwtChatChannelInterceptor(JwtUtils jwtUtils, UserDetailsServiceImpl userDetailsService) {
        this.jwtUtils = jwtUtils;
        this.userDetailsService = userDetailsService;
    }

    @Override
    public Message<?> preSend(Message<?> message, org.springframework.messaging.MessageChannel channel) {
        StompHeaderAccessor accessor = StompHeaderAccessor.wrap(message);
        StompCommand command = accessor.getCommand();

        if (command == null) {
            return message;
        }

        log.info("[STOMP] Comando: {}", command);

        // APENAS NO CONNECT: autenticar e configurar o usuário
        if (StompCommand.CONNECT.equals(command)) {
            // 1. Tentar token dos session attributes (via HandshakeInterceptor)
            String token = (String) accessor.getSessionAttributes().get("jwt_token");

            // 2. Se não tiver, tentar header Authorization (opcional)
            if (token == null) {
                String authHeader = accessor.getFirstNativeHeader("Authorization");
                if (authHeader != null && authHeader.startsWith("Bearer ")) {
                    token = authHeader.substring(7);
                }
            }

            if (token == null || token.isEmpty()) {
                log.warn("CONNECT sem token JWT");
                throw new MessagingException("Token ausente");
            }

            if (!jwtUtils.validateJwtToken(token)) {
                log.warn("Token JWT inválido ou expirado");
                throw new MessagingException("Token inválido");
            }

            String email = jwtUtils.getUserNameFromJwtToken(token);
            UserDetails userDetails = userDetailsService.loadUserByUsername(email);

            UsernamePasswordAuthenticationToken auth =
                    new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());

            // Define o usuário no accessor
            accessor.setUser(auth);

            // SALVA O SecurityContext para persistir entre frames (CRÍTICO!)
            accessor.getSessionAttributes().put(
                    "SPRING_SECURITY_CONTEXT",
                    new SecurityContextImpl(auth)
            );

            log.info("[SUCESSO] CONNECT autenticado como: {}", email);
        }


        return message;
    }

    @Override
    public void postSend(Message<?> message, org.springframework.messaging.MessageChannel channel, boolean sent) {
        if (!sent) return;

        StompHeaderAccessor accessor = StompHeaderAccessor.wrap(message);
        StompCommand command = accessor.getCommand();

        if (command == null) return;

        if (StompCommand.SUBSCRIBE.equals(command)) {
            if (accessor.getUser() != null) {
                log.info("[SUCESSO] SUBSCRIBE autenticado como: {} | Destino: {}",
                        accessor.getUser().getName(), accessor.getDestination());
            } else {
                log.warn("[ERRO] SUBSCRIBE sem usuário autenticado");
            }
        }

        if (StompCommand.SEND.equals(command)) {
            if (accessor.getUser() != null) {
                log.info("[SUCESSO] SEND autenticado como: {} | Destino: {}",
                        accessor.getUser().getName(), accessor.getDestination());
            }
        }
    }
}