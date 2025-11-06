package com.soulsurf.backend.config;

import com.soulsurf.backend.security.websocket.JwtChatChannelInterceptor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.ChannelRegistration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.*;

@Configuration
@EnableWebSocketMessageBroker
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

    private final JwtChatChannelInterceptor jwtChatChannelInterceptor;
    @Value("${app.cors.allowed-origins:*}")
    private String allowedOrigins;

    public WebSocketConfig(JwtChatChannelInterceptor jwtChatChannelInterceptor) {
        this.jwtChatChannelInterceptor = jwtChatChannelInterceptor;
    }

    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        // endpoint do handshake
        registry.addEndpoint("/ws").setAllowedOriginPatterns(allowedOrigins.split(","));
        registry.addEndpoint("/ws").setAllowedOriginPatterns(allowedOrigins.split(",")).withSockJS();
    }


    @Override
    public void configureClientInboundChannel(ChannelRegistration registration) {
        // aplica o interceptor em CONNECT/SUBSCRIBE/SEND do cliente
        registration.interceptors(jwtChatChannelInterceptor);
    }

    @Override
    public void configureMessageBroker(MessageBrokerRegistry registry) {
        // destinos para broadcast e fila pessoal
        registry.enableSimpleBroker("/topic", "/queue", "/user");
        registry.setApplicationDestinationPrefixes("/app");
        registry.setUserDestinationPrefix("/user");
    }
}
