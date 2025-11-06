package com.soulsurf.backend.controllers;

import com.soulsurf.backend.dto.MessageResponse;
import com.soulsurf.backend.dto.NotificationDTO;
import com.soulsurf.backend.security.service.UserDetailsImpl;
import com.soulsurf.backend.services.NotificationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import org.springframework.http.HttpStatus;
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
            if (userDetails == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(new MessageResponse("Usuário não autenticado"));
            }
            String username = userDetails.getUsername();
            List<NotificationDTO> notifications = notificationService.getUserNotifications(username);
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

    @Operation(summary = "Criar notificação de menção", description = "Cria uma notificação quando um usuário menciona outro em um comentário", security = @SecurityRequirement(name = "bearerAuth"))
    @ApiResponse(responseCode = "200", description = "Notificação criada com sucesso")
    @PostMapping("/mention")
    public ResponseEntity<?> createMentionNotification(
            @RequestParam String recipientUsername,
            @RequestParam Long postId,
            @RequestParam(value = "commentId", required = false) Long commentId,
            @AuthenticationPrincipal UserDetails userDetails) {

        if (userDetails == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(new MessageResponse("Usuário não autenticado"));
        }

        notificationService.createMentionNotification(
                userDetails.getUsername(),
                recipientUsername,
                postId,
                commentId
        );
        return ResponseEntity.ok(new MessageResponse("Notificação de menção criada"));
    }

    @Operation(summary = "Criar notificação de comentário", description = "Cria uma notificação quando alguém comenta em um post", security = @SecurityRequirement(name = "bearerAuth"))
    @ApiResponse(responseCode = "200", description = "Notificação criada com sucesso")
    @PostMapping("/comment")
    public ResponseEntity<?> createCommentNotification(
            @Parameter(description = "ID do post") @RequestParam Long postId,
            @Parameter(description = "ID do comentário") @RequestParam Long commentId,
            @AuthenticationPrincipal UserDetails userDetails) {
        try {
            notificationService.createCommentNotification(
                userDetails.getUsername(),
                postId,
                commentId
            );
            return ResponseEntity.ok(new MessageResponse("Notificação de comentário criada"));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(new MessageResponse("Erro ao criar notificação: " + e.getMessage()));
        }
    }

    @Operation(summary = "Criar notificação de resposta", description = "Cria uma notificação quando alguém responde a um comentário", security = @SecurityRequirement(name = "bearerAuth"))
    @ApiResponse(responseCode = "200", description = "Notificação criada com sucesso")
    @PostMapping("/reply")
    public ResponseEntity<?> createReplyNotification(
            @Parameter(description = "ID do post") @RequestParam Long postId,
            @Parameter(description = "ID do comentário") @RequestParam Long commentId,
            @Parameter(description = "ID do comentário pai") @RequestParam Long parentCommentId,
            @AuthenticationPrincipal UserDetails userDetails) {
        try {
            notificationService.createReplyNotification(
                userDetails.getUsername(),
                postId,
                commentId,
                parentCommentId
            );
            return ResponseEntity.ok(new MessageResponse("Notificação de resposta criada"));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(new MessageResponse("Erro ao criar notificação: " + e.getMessage()));
        }
    }
}
