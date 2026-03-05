package com.soulsurf.backend.modules.notification.controller;

import com.soulsurf.backend.modules.chat.dto.MessageResponse;
import com.soulsurf.backend.modules.notification.dto.NotificationDTO;
import com.soulsurf.backend.core.security.service.UserDetailsImpl;
import com.soulsurf.backend.modules.notification.service.NotificationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

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
    public ResponseEntity<List<NotificationDTO>> getUserNotifications(
            @AuthenticationPrincipal UserDetails userDetails) {
        List<NotificationDTO> notifications = notificationService.getUserNotifications(userDetails.getUsername());
        return ResponseEntity.ok(notifications);
    }

    @Operation(summary = "Contar notificações não lidas", description = "Retorna o número de notificações não lidas do usuário autenticado", security = @SecurityRequirement(name = "bearerAuth"))
    @ApiResponse(responseCode = "200", description = "Contagem realizada com sucesso")
    @GetMapping("/count")
    public ResponseEntity<Map<String, Integer>> getUnreadCount(@AuthenticationPrincipal UserDetails userDetails) {
        int count = notificationService.getUnreadCount(userDetails.getUsername());
        return ResponseEntity.ok(Map.of("count", count));
    }

    @Operation(summary = "Marcar notificação como lida", description = "Marca uma notificação específica como lida", security = @SecurityRequirement(name = "bearerAuth"))
    @ApiResponse(responseCode = "200", description = "Notificação marcada como lida com sucesso")
    @ApiResponse(responseCode = "404", description = "Notificação não encontrada")
    @PutMapping("/{id}/read")
    public ResponseEntity<MessageResponse> markAsRead(
            @Parameter(description = "ID da notificação") @PathVariable Long id,
            @AuthenticationPrincipal UserDetails userDetails) {
        notificationService.markAsRead(id, userDetails.getUsername());
        return ResponseEntity.ok(new MessageResponse("Notificação marcada como lida"));
    }

    @Operation(summary = "Criar notificação de menção", description = "Cria uma notificação quando um usuário menciona outro em um comentário", security = @SecurityRequirement(name = "bearerAuth"))
    @ApiResponse(responseCode = "200", description = "Notificação criada com sucesso")
    @PostMapping("/mention")
    public ResponseEntity<MessageResponse> createMentionNotification(
            @RequestParam String recipientUsername,
            @RequestParam Long postId,
            @RequestParam(value = "commentId", required = false) Long commentId,
            @AuthenticationPrincipal UserDetails userDetails) {

        notificationService.createMentionNotification(
                userDetails.getUsername(),
                recipientUsername,
                postId,
                commentId);
        return ResponseEntity.ok(new MessageResponse("Notificação de menção criada"));
    }

    @Operation(summary = "Criar notificação de comentário", description = "Cria uma notificação quando alguém comenta em um post", security = @SecurityRequirement(name = "bearerAuth"))
    @ApiResponse(responseCode = "200", description = "Notificação criada com sucesso")
    @PostMapping("/comment")
    public ResponseEntity<MessageResponse> createCommentNotification(
            @Parameter(description = "ID do post") @RequestParam Long postId,
            @Parameter(description = "ID do comentário") @RequestParam Long commentId,
            @AuthenticationPrincipal UserDetails userDetails) {
        notificationService.createCommentNotification(
                userDetails.getUsername(),
                postId,
                commentId);
        return ResponseEntity.ok(new MessageResponse("Notificação de comentário criada"));
    }

    @Operation(summary = "Criar notificação de resposta", description = "Cria uma notificação quando alguém responde a um comentário", security = @SecurityRequirement(name = "bearerAuth"))
    @ApiResponse(responseCode = "200", description = "Notificação criada com sucesso")
    @PostMapping("/reply")
    public ResponseEntity<MessageResponse> createReplyNotification(
            @Parameter(description = "ID do post") @RequestParam Long postId,
            @Parameter(description = "ID do comentário") @RequestParam Long commentId,
            @Parameter(description = "ID do comentário pai") @RequestParam Long parentCommentId,
            @AuthenticationPrincipal UserDetails userDetails) {
        notificationService.createReplyNotification(
                userDetails.getUsername(),
                postId,
                commentId,
                parentCommentId);
        return ResponseEntity.ok(new MessageResponse("Notificação de resposta criada"));
    }
}
