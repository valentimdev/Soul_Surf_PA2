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
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.reactive.function.client.WebClient;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Service
@Slf4j
public class PushNotificationService {

    private static final String EXPO_PUSH_URL = "https://exp.host/--/api/v2/push/send";
    private static final String DEVICE_NOT_REGISTERED = "DeviceNotRegistered";

    private final PushTokenRepository pushTokenRepository;
    private final UserRepository userRepository;
    private final WebClient webClient;

    public PushNotificationService(PushTokenRepository pushTokenRepository, UserRepository userRepository) {
        this.pushTokenRepository = pushTokenRepository;
        this.userRepository = userRepository;
        this.webClient = WebClient.builder()
                .baseUrl(EXPO_PUSH_URL)
                .build();
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

        List<ExpoPushMessage> messages = tokens.stream()
                .map(token -> new ExpoPushMessage(token.getToken(), title, body, data))
                .toList();

        ExpoPushResponse response = webClient.post()
                .bodyValue(messages)
                .retrieve()
                .bodyToMono(ExpoPushResponse.class)
                .block();

        LocalDateTime now = LocalDateTime.now();
        tokens.forEach(token -> token.setLastUsedAt(now));

        deactivateInvalidTokens(tokens, response);
        pushTokenRepository.saveAll(tokens);

        return tokens.size();
    }

    private void deactivateInvalidTokens(List<PushToken> tokens, ExpoPushResponse response) {
        if (response == null || response.getData() == null) {
            return;
        }

        List<ExpoPushTicket> tickets = response.getData();
        int limit = Math.min(tokens.size(), tickets.size());

        for (int i = 0; i < limit; i++) {
            ExpoPushTicket ticket = tickets.get(i);
            if (!"error".equalsIgnoreCase(ticket.getStatus())) {
                continue;
            }

            Object expoError = ticket.getDetails() == null ? null : ticket.getDetails().get("error");
            if (DEVICE_NOT_REGISTERED.equals(expoError)) {
                PushToken token = tokens.get(i);
                token.setActive(false);
                log.info("Push token marked inactive: tokenId={}, reason={}", token.getId(), expoError);
            }
        }
    }

    @Getter
    private static class ExpoPushMessage {
        private final String to;
        private final String title;
        private final String body;
        private final Map<String, Object> data;

        private ExpoPushMessage(String to, String title, String body, Map<String, Object> data) {
            this.to = to;
            this.title = title;
            this.body = body;
            this.data = data == null ? Map.of() : data;
        }
    }

    @Getter
    @Setter
    private static class ExpoPushResponse {
        private List<ExpoPushTicket> data;
    }

    @Getter
    @Setter
    private static class ExpoPushTicket {
        private String status;
        private String id;
        private String message;
        private Map<String, Object> details;
    }
}
