package com.soulsurf.backend.core.config;

import com.soulsurf.backend.core.security.websocket.JwtChatChannelInterceptor;
import com.soulsurf.backend.core.security.websocket.JwtHandshakeInterceptor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.ChannelRegistration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

import java.util.List;

@Configuration
@EnableWebSocketMessageBroker
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {
    private static final String[] SAFE_DEFAULT_ORIGINS = {
            "http://localhost:8081",
            "http://localhost:3000",
            "http://localhost:5173"
    };

    private final JwtChatChannelInterceptor jwtChatChannelInterceptor;
    private final JwtHandshakeInterceptor jwtHandshakeInterceptor;
    private final String allowedOrigins;

    public WebSocketConfig(
            JwtChatChannelInterceptor jwtChatChannelInterceptor,
            JwtHandshakeInterceptor jwtHandshakeInterceptor,
            @Value("${app.cors.allowed-origins:http://localhost:8081,http://localhost:3000}") String allowedOrigins) {
        this.jwtChatChannelInterceptor = jwtChatChannelInterceptor;
        this.jwtHandshakeInterceptor = jwtHandshakeInterceptor;
        this.allowedOrigins = allowedOrigins;
    }

    @Override
    public void configureMessageBroker(MessageBrokerRegistry config) {
        config.enableSimpleBroker("/topic");
        config.setApplicationDestinationPrefixes("/app");
    }

    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        String[] originPatterns = resolveAllowedOrigins();

        registry.addEndpoint("/ws")
                .setAllowedOriginPatterns(originPatterns)
                .addInterceptors(jwtHandshakeInterceptor);

        registry.addEndpoint("/ws/sockjs")
                .setAllowedOriginPatterns(originPatterns)
                .addInterceptors(jwtHandshakeInterceptor)
                .withSockJS();
    }

    @Override
    public void configureClientInboundChannel(ChannelRegistration registration) {
        registration.interceptors(jwtChatChannelInterceptor);
    }

    private String[] resolveAllowedOrigins() {
        List<String> patterns = List.of(allowedOrigins.split(","))
                .stream()
                .map(String::trim)
                .filter(origin -> !origin.isBlank())
                .filter(origin -> !origin.equals("*"))
                .toList();

        if (patterns.isEmpty()) {
            return SAFE_DEFAULT_ORIGINS;
        }
        return patterns.toArray(String[]::new);
    }
}
