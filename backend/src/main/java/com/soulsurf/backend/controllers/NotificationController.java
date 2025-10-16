package com.soulsurf.backend.controllers;

import com.soulsurf.backend.dto.MessageResponse;
import com.soulsurf.backend.dto.NotificationDTO;
import com.soulsurf.backend.services.NotificationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@CrossOrigin(origins = "*", maxAge = 3600)
@RestController
@RequestMapping("/api/notifications")
public class NotificationController {

    private final NotificationService notificationService;

    public NotificationController(NotificationService notificationService) {
        this.notificationService = notificationService;
    }

    @Operation(summary = "Obter notificações do usuário", description = "Retorna todas as notificações do usuário autenticado", security = @SecurityRequirement(name = "bearerAuth"))
    @ApiResponse(responseCode = "200", description = "Notificações encontradas com sucesso")
    @GetMapping("/")
    public ResponseEntity<?> getUserNotifications(@AuthenticationPrincipal UserDetails userDetails) {
        try {
            List<NotificationDTO> notifications = notificationService.getUserNotifications(userDetails.getUsername());
            return ResponseEntity.ok(notifications);
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(new MessageResponse("Erro ao buscar notificações: " + e.getMessage()));
        }
    }

    @Operation(summary = "Contar notificações não lidas", description = "Retorna o número de notificações não lidas do usuário autenticado", security = @SecurityRequirement(name = "bearerAuth"))
    @ApiResponse(responseCode = "200", description = "Contagem realizada com sucesso")
    @GetMapping("/count")
    public ResponseEntity<?> getUnreadCount(@AuthenticationPrincipal UserDetails userDetails) {
        try {
            int count = notificationService.getUnreadCount(userDetails.getUsername());
            Map<String, Integer> response = new HashMap<>();
            response.put("count", count);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(new MessageResponse("Erro ao contar notificações: " + e.getMessage()));
        }
    }

    @Operation(summary = "Marcar notificação como lida", description = "Marca uma notificação específica como lida", security = @SecurityRequirement(name = "bearerAuth"))
    @ApiResponse(responseCode = "200", description = "Notificação marcada como lida com sucesso")
    @ApiResponse(responseCode = "404", description = "Notificação não encontrada")
    @PutMapping("/{id}/read")
    public ResponseEntity<?> markAsRead(
            @Parameter(description = "ID da notificação") @PathVariable Long id,
            @AuthenticationPrincipal UserDetails userDetails) {
        try {
            notificationService.markAsRead(id, userDetails.getUsername());
            return ResponseEntity.ok(new MessageResponse("Notificação marcada como lida"));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(new MessageResponse("Erro ao marcar notificação como lida: " + e.getMessage()));
        }
    }
}
