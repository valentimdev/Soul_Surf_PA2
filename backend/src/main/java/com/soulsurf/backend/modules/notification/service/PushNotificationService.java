package com.soulsurf.backend.modules.notification.service;

import com.soulsurf.backend.modules.notification.dto.RegisterDeviceTokenRequest;
import com.soulsurf.backend.modules.notification.dto.SendPushNotificationRequest;
import com.soulsurf.backend.modules.notification.entity.PushToken;
import com.soulsurf.backend.modules.notification.repository.PushTokenRepository;
import com.soulsurf.backend.modules.user.entity.User;
import com.soulsurf.backend.modules.user.repository.UserRepository;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientException;
import org.springframework.web.reactive.function.client.WebClientResponseException;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
@Slf4j
public class PushNotificationService {

    private static final String EXPO_PUSH_BASE_URL = "https://exp.host";
    private static final String EXPO_PUSH_SEND_PATH = "/--/api/v2/push/send";
    private static final String DEFAULT_ANDROID_CHANNEL_ID = "default";
    private static final String DEFAULT_IOS_SOUND = "default";
    private static final String HIGH_PRIORITY = "high";
    private static final String DEVICE_NOT_REGISTERED = "DeviceNotRegistered";

    private final PushTokenRepository pushTokenRepository;
    private final UserRepository userRepository;
    private final WebClient webClient;

    @Autowired
    public PushNotificationService(
            PushTokenRepository pushTokenRepository,
            UserRepository userRepository,
            WebClient.Builder webClientBuilder) {
        this(pushTokenRepository, userRepository, webClientBuilder
                .baseUrl(EXPO_PUSH_BASE_URL)
                .build());
    }

    PushNotificationService(
            PushTokenRepository pushTokenRepository,
            UserRepository userRepository,
            WebClient webClient) {
        this.pushTokenRepository = pushTokenRepository;
        this.userRepository = userRepository;
        this.webClient = webClient;
    }

    @Transactional
    public void registerDeviceToken(String userEmail, RegisterDeviceTokenRequest request) {
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new UsernameNotFoundException("Usuario nao encontrado: " + userEmail));

        String token = request.getToken().trim();
        String platform = request.getPlatform().trim().toLowerCase();

        PushToken pushToken = pushTokenRepository.findByToken(token)
                .orElseGet(PushToken::new);

        pushToken.setUser(user);
        pushToken.setToken(token);
        pushToken.setPlatform(platform);
        pushToken.setActive(true);

        pushTokenRepository.save(pushToken);
    }

    @Transactional
    public int sendToUser(String senderEmail, SendPushNotificationRequest request) {
        User sender = userRepository.findByEmail(senderEmail)
                .orElseThrow(() -> new UsernameNotFoundException("Usuario remetente nao encontrado: " + senderEmail));

        User recipient = userRepository.findByUsername(request.getTargetUsername())
                .orElseThrow(() -> new UsernameNotFoundException(
                        "Usuario destino nao encontrado: " + request.getTargetUsername()));

        return sendToUser(recipient, request.getTitle(), request.getBody(), Map.of(
                "senderId", sender.getId(),
                "senderUsername", sender.getUsername()
        ));
    }

    @Transactional
    public int sendToUser(User recipient, String title, String body, Map<String, Object> data) {
        List<PushToken> tokens = pushTokenRepository.findByUserAndActiveTrue(recipient);

        if (tokens.isEmpty()) {
            return 0;
        }

        LocalDateTime now = LocalDateTime.now();
        List<PushToken> updatedTokens = new ArrayList<>();
        int accepted = 0;

        for (PushToken token : tokens) {
            TokenPushResult result = sendToToken(recipient, token, title, body, data, now);
            accepted += result.accepted;

            if (result.tokenUpdated) {
                updatedTokens.add(token);
            }
        }

        if (!updatedTokens.isEmpty()) {
            pushTokenRepository.saveAll(updatedTokens);
        }

        return accepted;
    }

    private TokenPushResult sendToToken(
            User recipient,
            PushToken token,
            String title,
            String body,
            Map<String, Object> data,
            LocalDateTime now) {
        ExpoPushResponse response;
        try {
            response = webClient.post()
                    .uri(EXPO_PUSH_SEND_PATH)
                    .contentType(MediaType.APPLICATION_JSON)
                    .accept(MediaType.APPLICATION_JSON)
                    .bodyValue(List.of(new ExpoPushMessage(token.getToken(), title, body, data)))
                    .retrieve()
                    .bodyToMono(ExpoPushResponse.class)
                    .block();
        } catch (WebClientResponseException e) {
            log.warn(
                    "Expo push request failed: recipient={}, tokenId={}, status={}, body={}",
                    recipient.getUsername(),
                    token.getId(),
                    e.getStatusCode(),
                    e.getResponseBodyAsString());
            return TokenPushResult.notUpdated();
        } catch (WebClientException e) {
            log.warn(
                    "Expo push request failed: recipient={}, tokenId={}, message={}",
                    recipient.getUsername(),
                    token.getId(),
                    e.getMessage());
            return TokenPushResult.notUpdated();
        }

        token.setLastUsedAt(now);
        return new TokenPushResult(handleExpoResponse(recipient, List.of(token), response), true);
    }

    private int handleExpoResponse(User recipient, List<PushToken> tokens, ExpoPushResponse response) {
        if (response == null) {
            log.warn("Expo push returned empty response: recipient={}", recipient.getUsername());
            return 0;
        }

        logRequestErrors(recipient, response);

        if (response.getData() == null) {
            return 0;
        }

        List<ExpoPushTicket> tickets = response.getData();
        int limit = Math.min(tokens.size(), tickets.size());
        int accepted = 0;

        for (int i = 0; i < limit; i++) {
            ExpoPushTicket ticket = tickets.get(i);

            if ("ok".equalsIgnoreCase(ticket.getStatus())) {
                accepted++;
                continue;
            }

            if (!"error".equalsIgnoreCase(ticket.getStatus())) {
                log.warn(
                        "Expo push returned unknown ticket status: recipient={}, tokenId={}, status={}, message={}",
                        recipient.getUsername(),
                        tokens.get(i).getId(),
                        ticket.getStatus(),
                        ticket.getMessage());
                continue;
            }

            PushToken token = tokens.get(i);
            Object expoError = ticket.getDetails() == null ? null : ticket.getDetails().get("error");
            log.warn(
                    "Expo push ticket error: recipient={}, tokenId={}, error={}, message={}",
                    recipient.getUsername(),
                    token.getId(),
                    expoError,
                    ticket.getMessage());

            if (DEVICE_NOT_REGISTERED.equals(expoError)) {
                token.setActive(false);
                log.info("Push token marked inactive: tokenId={}, reason={}", token.getId(), expoError);
            }
        }

        if (tickets.size() != tokens.size()) {
            log.warn(
                    "Expo push ticket count mismatch: recipient={}, tokens={}, tickets={}",
                    recipient.getUsername(),
                    tokens.size(),
                    tickets.size());
        }

        return accepted;
    }

    private void logRequestErrors(User recipient, ExpoPushResponse response) {
        if (response.getErrors() == null || response.getErrors().isEmpty()) {
            return;
        }

        response.getErrors().forEach(error -> log.error(
                "Expo push request error: recipient={}, code={}, message={}",
                recipient.getUsername(),
                error.getCode(),
                error.getMessage()));
    }

    @Getter
    private static class ExpoPushMessage {
        private final String to;
        private final String title;
        private final String body;
        private final Map<String, Object> data;
        private final String sound;
        private final String channelId;
        private final String priority;

        private ExpoPushMessage(String to, String title, String body, Map<String, Object> data) {
            this.to = to;
            this.title = title;
            this.body = body;
            this.data = data == null ? Map.of() : data;
            this.sound = DEFAULT_IOS_SOUND;
            this.channelId = DEFAULT_ANDROID_CHANNEL_ID;
            this.priority = HIGH_PRIORITY;
        }
    }

    @Getter
    @Setter
    private static class ExpoPushResponse {
        private List<ExpoPushTicket> data;
        private List<ExpoPushRequestError> errors;
    }

    @Getter
    @Setter
    private static class ExpoPushTicket {
        private String status;
        private String id;
        private String message;
        private Map<String, Object> details;
    }

    @Getter
    @Setter
    private static class ExpoPushRequestError {
        private String code;
        private String message;
    }

    private static class TokenPushResult {
        private final int accepted;
        private final boolean tokenUpdated;

        private TokenPushResult(int accepted, boolean tokenUpdated) {
            this.accepted = accepted;
            this.tokenUpdated = tokenUpdated;
        }

        private static TokenPushResult notUpdated() {
            return new TokenPushResult(0, false);
        }
    }
}
