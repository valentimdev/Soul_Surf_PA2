package com.soulsurf.backend.modules.notification.service;

import com.soulsurf.backend.modules.notification.dto.RegisterDeviceTokenRequest;
import com.soulsurf.backend.modules.notification.entity.PushToken;
import com.soulsurf.backend.modules.notification.repository.PushTokenRepository;
import com.soulsurf.backend.modules.user.entity.User;
import com.soulsurf.backend.modules.user.repository.UserRepository;
import com.sun.net.httpserver.HttpServer;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.reactive.function.client.WebClient;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PushNotificationServiceTest {

    @Mock
    private PushTokenRepository pushTokenRepository;

    @Mock
    private UserRepository userRepository;

    private HttpServer expoServer;

    @AfterEach
    void tearDown() {
        if (expoServer != null) {
            expoServer.stop(0);
        }
    }

    @Test
    void registerDeviceTokenShouldTrimAndReactivateExistingToken() {
        User user = new User();
        user.setId(1L);
        user.setEmail("surfer@example.com");

        PushToken existingToken = new PushToken();
        existingToken.setActive(false);
        existingToken.setPlatform("ios");

        RegisterDeviceTokenRequest request = new RegisterDeviceTokenRequest();
        request.setToken("  ExpoPushToken[abc]  ");
        request.setPlatform(" Android ");

        when(userRepository.findByEmail("surfer@example.com")).thenReturn(Optional.of(user));
        when(pushTokenRepository.findByToken("ExpoPushToken[abc]")).thenReturn(Optional.of(existingToken));

        PushNotificationService service = new PushNotificationService(
                pushTokenRepository,
                userRepository,
                WebClient.builder().baseUrl("http://127.0.0.1").build());

        service.registerDeviceToken("surfer@example.com", request);

        assertThat(existingToken.getUser()).isSameAs(user);
        assertThat(existingToken.getToken()).isEqualTo("ExpoPushToken[abc]");
        assertThat(existingToken.getPlatform()).isEqualTo("android");
        assertThat(existingToken.isActive()).isTrue();
        verify(pushTokenRepository).save(existingToken);
    }

    @Test
    void sendToUserShouldUseExpoPushEndpointAndDeactivateInvalidTokens() throws IOException {
        AtomicReference<String> requestMethod = new AtomicReference<>();
        AtomicReference<String> requestPath = new AtomicReference<>();
        AtomicReference<String> requestBody = new AtomicReference<>();

        PushNotificationService service = serviceWithExpoResponse(
                """
                {
                  "data": [
                    { "status": "ok", "id": "ticket-ok" },
                    {
                      "status": "error",
                      "message": "Device is not registered",
                      "details": { "error": "DeviceNotRegistered" }
                    }
                  ]
                }
                """,
                requestMethod,
                requestPath,
                requestBody);

        User recipient = new User();
        recipient.setId(7L);
        recipient.setUsername("target");

        PushToken validToken = pushToken(10L, "ExpoPushToken[ok]");
        PushToken invalidToken = pushToken(11L, "ExpoPushToken[invalid]");
        List<PushToken> tokens = List.of(validToken, invalidToken);

        when(pushTokenRepository.findByUserAndActiveTrue(recipient)).thenReturn(tokens);

        int accepted = service.sendToUser(recipient, "Soul Surf", "Nova onda", Map.of("type", "LIKE"));

        assertThat(accepted).isEqualTo(1);
        assertThat(requestMethod.get()).isEqualTo("POST");
        assertThat(requestPath.get()).isEqualTo("/--/api/v2/push/send");
        assertThat(requestBody.get())
                .contains("\"to\":\"ExpoPushToken[ok]\"")
                .contains("\"title\":\"Soul Surf\"")
                .contains("\"body\":\"Nova onda\"")
                .contains("\"type\":\"LIKE\"")
                .contains("\"sound\":\"default\"")
                .contains("\"channelId\":\"default\"")
                .contains("\"priority\":\"high\"");
        assertThat(validToken.getLastUsedAt()).isNotNull();
        assertThat(invalidToken.getLastUsedAt()).isNotNull();
        assertThat(invalidToken.isActive()).isFalse();
        verify(pushTokenRepository).saveAll(tokens);
    }

    private PushNotificationService serviceWithExpoResponse(
            String responseBody,
            AtomicReference<String> requestMethod,
            AtomicReference<String> requestPath,
            AtomicReference<String> requestBody) throws IOException {
        expoServer = HttpServer.create(new InetSocketAddress("127.0.0.1", 0), 0);
        expoServer.createContext("/--/api/v2/push/send", exchange -> {
            requestMethod.set(exchange.getRequestMethod());
            requestPath.set(exchange.getRequestURI().getPath());
            requestBody.set(new String(exchange.getRequestBody().readAllBytes(), StandardCharsets.UTF_8));

            byte[] bytes = responseBody.getBytes(StandardCharsets.UTF_8);
            exchange.getResponseHeaders().add("Content-Type", "application/json");
            exchange.sendResponseHeaders(200, bytes.length);
            exchange.getResponseBody().write(bytes);
            exchange.close();
        });
        expoServer.start();

        String baseUrl = "http://127.0.0.1:" + expoServer.getAddress().getPort();
        return new PushNotificationService(
                pushTokenRepository,
                userRepository,
                WebClient.builder().baseUrl(baseUrl).build());
    }

    private static PushToken pushToken(Long id, String token) {
        PushToken pushToken = new PushToken();
        pushToken.setId(id);
        pushToken.setToken(token);
        pushToken.setPlatform("android");
        pushToken.setActive(true);
        return pushToken;
    }
}
