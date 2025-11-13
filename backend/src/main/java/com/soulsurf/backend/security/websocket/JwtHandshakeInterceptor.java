// src/main/java/com/soulsurf/backend/security/websocket/JwtHandshakeInterceptor.java
package com.soulsurf.backend.security.websocket;

import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.http.server.ServletServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.server.HandshakeInterceptor;

import java.util.Map;

@Component
public class JwtHandshakeInterceptor implements HandshakeInterceptor {

    @Override
    public boolean beforeHandshake(ServerHttpRequest request,
                                   ServerHttpResponse response,
                                   WebSocketHandler wsHandler,
                                   Map<String, Object> attributes) {

        if (request instanceof ServletServerHttpRequest servletRequest) {
            String query = servletRequest.getURI().getQuery();
            if (query != null && query.contains("access_token=")) {
                String token = query.split("access_token=")[1].split("&")[0];
                attributes.put("jwt_token", token); // Nome usado no ChannelInterceptor
                return true;
            }
        }
        return true; // Continua mesmo sem token (será rejeitado no interceptor)
    }

    @Override
    public void afterHandshake(ServerHttpRequest request,
                               ServerHttpResponse response,
                               WebSocketHandler wsHandler,
                               Exception exception) {
        // Nada aqui
    }
}