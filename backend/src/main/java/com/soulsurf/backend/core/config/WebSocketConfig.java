// src/main/java/com/soulsurf/backend/config/WebSocketConfig.java
package com.soulsurf.backend.core.config;

import com.soulsurf.backend.core.security.websocket.JwtChatChannelInterceptor;
import com.soulsurf.backend.core.security.websocket.JwtHandshakeInterceptor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.ChannelRegistration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

@Configuration
@EnableWebSocketMessageBroker
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

    private final JwtChatChannelInterceptor jwtChatChannelInterceptor;
    private final JwtHandshakeInterceptor jwtHandshakeInterceptor;

    public WebSocketConfig(JwtChatChannelInterceptor jwtChatChannelInterceptor,
                           JwtHandshakeInterceptor jwtHandshakeInterceptor) {
        this.jwtChatChannelInterceptor = jwtChatChannelInterceptor;
        this.jwtHandshakeInterceptor = jwtHandshakeInterceptor;
    }

    @Override
    public void configureMessageBroker(MessageBrokerRegistry config) {
        config.enableSimpleBroker("/topic");
        config.setApplicationDestinationPrefixes("/app");
    }

    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        // WEBSOCKET PURO (OBRIGATÓRIO)
        registry.addEndpoint("/ws")
                .setAllowedOriginPatterns("*")
                .addInterceptors(jwtHandshakeInterceptor);

        // OPCIONAL: SockJS em outro endpoint
        registry.addEndpoint("/ws/sockjs")
                .setAllowedOriginPatterns("*")
                .addInterceptors(jwtHandshakeInterceptor)
                .withSockJS();
    }

    @Override
    public void configureClientInboundChannel(ChannelRegistration registration) {
        registration.interceptors(jwtChatChannelInterceptor);
    }
}
