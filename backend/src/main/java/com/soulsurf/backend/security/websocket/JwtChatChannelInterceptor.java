package com.soulsurf.backend.security.websocket;

import com.soulsurf.backend.entities.ConversationParticipantId;
import com.soulsurf.backend.repository.ConversationParticipantRepository;
import com.soulsurf.backend.security.jwt.JwtUtils;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.MessagingException;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

@Component
public class JwtChatChannelInterceptor implements ChannelInterceptor {

    private final JwtUtils jwtUtils;
    private final ConversationParticipantRepository participantRepo;

    public JwtChatChannelInterceptor(JwtUtils jwtUtils,
                                     ConversationParticipantRepository participantRepo) {
        this.jwtUtils = jwtUtils;
        this.participantRepo = participantRepo;
    }

    @Override
    public Message<?> preSend(Message<?> message, MessageChannel channel) {
        StompHeaderAccessor acc = StompHeaderAccessor.wrap(message);
        StompCommand cmd = acc.getCommand();

        if (StompCommand.CONNECT.equals(cmd)) {
            // JWT no header nativo STOMP
            String auth = acc.getFirstNativeHeader("Authorization");
            if (!StringUtils.hasText(auth) || !auth.startsWith("Bearer ")) {
                throw new AuthException("Missing Authorization");
            }
            String token = auth.substring(7);
            if (!jwtUtils.validateJwtToken(token)) {
                throw new AuthException("Invalid token");
            }
            // use o método que retorna o identificador do usuário (id ou email)
            String userId = jwtUtils.getUserNameFromJwtToken(token);
            acc.setUser(() -> userId); // Principal.getName() = userId
        }

        if (StompCommand.SUBSCRIBE.equals(cmd)) {
            String destination = acc.getDestination(); // ex: /topic/conversations/{id}
            String me = acc.getUser() != null ? acc.getUser().getName() : null;

            if (destination != null && destination.startsWith("/topic/conversations/")) {
                String conversationId = destination.substring("/topic/conversations/".length());
                boolean isParticipant = participantRepo
                        .findById(new ConversationParticipantId(conversationId, me))
                        .isPresent();
                if (!isParticipant) {
                    throw new AccessDenied("Not a participant of this conversation");
                }
            }
        }

        return message;
    }

    private static class AuthException extends MessagingException {
        public AuthException(String msg) { super(msg); }
    }
    private static class AccessDenied extends MessagingException {
        public AccessDenied(String msg) { super(msg); }
    }
}
