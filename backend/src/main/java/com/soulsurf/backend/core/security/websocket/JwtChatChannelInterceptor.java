package com.soulsurf.backend.core.security.websocket;

import com.soulsurf.backend.core.security.jwt.JwtUtils;
import com.soulsurf.backend.core.security.service.UserDetailsServiceImpl;
import com.soulsurf.backend.modules.chat.entity.ConversationParticipantId;
import com.soulsurf.backend.modules.chat.repository.ConversationParticipantRepository;
import com.soulsurf.backend.modules.user.repository.UserRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessagingException;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextImpl;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import java.util.Map;

@Slf4j
@Component
public class JwtChatChannelInterceptor implements ChannelInterceptor {

    private static final String CONVERSATION_TOPIC_PREFIX = "/topic/conversations/";
    private static final String NOTIFICATION_TOPIC_PREFIX = "/topic/notifications/";

    private final JwtUtils jwtUtils;
    private final UserDetailsServiceImpl userDetailsService;
    private final ConversationParticipantRepository conversationParticipantRepository;
    private final UserRepository userRepository;

    public JwtChatChannelInterceptor(
            JwtUtils jwtUtils,
            UserDetailsServiceImpl userDetailsService,
            ConversationParticipantRepository conversationParticipantRepository,
            UserRepository userRepository) {
        this.jwtUtils = jwtUtils;
        this.userDetailsService = userDetailsService;
        this.conversationParticipantRepository = conversationParticipantRepository;
        this.userRepository = userRepository;
    }

    @Override
    public Message<?> preSend(Message<?> message, org.springframework.messaging.MessageChannel channel) {
        StompHeaderAccessor accessor = StompHeaderAccessor.wrap(message);
        StompCommand command = accessor.getCommand();

        if (command == null) {
            return message;
        }

        if (StompCommand.CONNECT.equals(command)) {
            authenticateConnect(accessor);
            return message;
        }

        if ((StompCommand.SUBSCRIBE.equals(command) || StompCommand.SEND.equals(command))
                && accessor.getUser() == null) {
            throw new MessagingException("Usuario nao autenticado");
        }

        if (StompCommand.SUBSCRIBE.equals(command)) {
            authorizeSubscription(accessor);
        }

        return message;
    }

    private void authenticateConnect(StompHeaderAccessor accessor) {
        String token = readTokenFromSession(accessor.getSessionAttributes());

        if (token == null) {
            String authHeader = accessor.getFirstNativeHeader("Authorization");
            if (authHeader != null && authHeader.startsWith("Bearer ")) {
                token = authHeader.substring(7);
            }
        }

        if (token == null || token.isBlank()) {
            throw new MessagingException("Token ausente");
        }

        if (!jwtUtils.validateJwtToken(token)) {
            throw new MessagingException("Token invalido");
        }

        String email = jwtUtils.getUserNameFromJwtToken(token);
        UserDetails userDetails = userDetailsService.loadUserByUsername(email);

        UsernamePasswordAuthenticationToken auth =
                new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());

        accessor.setUser(auth);

        Map<String, Object> sessionAttributes = accessor.getSessionAttributes();
        if (sessionAttributes != null) {
            sessionAttributes.put("SPRING_SECURITY_CONTEXT", new SecurityContextImpl(auth));
        }

        log.debug("[STOMP] CONNECT autenticado: {}", email);
    }

    private String readTokenFromSession(Map<String, Object> sessionAttributes) {
        if (sessionAttributes == null) {
            return null;
        }
        Object value = sessionAttributes.get("jwt_token");
        if (value instanceof String token && !token.isBlank()) {
            return token;
        }
        return null;
    }

    private void authorizeSubscription(StompHeaderAccessor accessor) {
        if (accessor.getDestination() == null || accessor.getUser() == null) {
            return;
        }

        String destination = accessor.getDestination();
        String currentUserEmail = accessor.getUser().getName();

        if (destination.startsWith(CONVERSATION_TOPIC_PREFIX)) {
            String conversationId = extractDestinationSegment(destination, CONVERSATION_TOPIC_PREFIX);
            if (conversationId.isBlank()) {
                throw new AccessDeniedException("Conversa invalida");
            }

            ConversationParticipantId participantId = new ConversationParticipantId(conversationId, currentUserEmail);
            boolean isParticipant = conversationParticipantRepository.findById(participantId).isPresent();
            if (!isParticipant) {
                throw new AccessDeniedException("Usuario nao participa da conversa");
            }
            return;
        }

        if (destination.startsWith(NOTIFICATION_TOPIC_PREFIX)) {
            String requestedUsername = extractDestinationSegment(destination, NOTIFICATION_TOPIC_PREFIX);
            if (requestedUsername.isBlank()) {
                throw new AccessDeniedException("Destino de notificacao invalido");
            }

            String currentUsername = userRepository.findByEmail(currentUserEmail)
                    .map(user -> user.getUsername())
                    .orElseThrow(() -> new AccessDeniedException("Usuario autenticado nao encontrado"));

            if (!currentUsername.equalsIgnoreCase(requestedUsername)) {
                throw new AccessDeniedException("Acesso negado ao canal de notificacao");
            }
        }
    }

    private String extractDestinationSegment(String destination, String prefix) {
        String raw = destination.substring(prefix.length()).trim();
        int slashIndex = raw.indexOf('/');
        return slashIndex >= 0 ? raw.substring(0, slashIndex) : raw;
    }
}
