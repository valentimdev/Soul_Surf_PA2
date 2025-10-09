package com.soulsurf.backend.controllers;

import com.soulsurf.backend.dto.MessageResponse;
import com.soulsurf.backend.services.AdminService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin")
@Tag(name = "0. Administração", description = "Endpoints de administração do sistema")
public class AdminController {

    private final AdminService adminService;

    public AdminController(AdminService adminService) {
        this.adminService = adminService;
    }

    @DeleteMapping("/users/{userId}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Apaga um usuário")
    public ResponseEntity<MessageResponse> deleteUser(@PathVariable Long userId,
                                                     @AuthenticationPrincipal UserDetails actor) {
        adminService.deleteUser(userId, actor.getUsername());
        return ResponseEntity.ok(new MessageResponse("Usuário removido"));
    }

    @DeleteMapping("/posts/{postId}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Apaga um post")
    public ResponseEntity<MessageResponse> deletePost(@PathVariable Long postId,
                                                     @AuthenticationPrincipal UserDetails actor) {
        adminService.deletePost(postId, actor.getUsername());
        return ResponseEntity.ok(new MessageResponse("Post removido"));
    }

    @DeleteMapping("/comments/{commentId}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Apaga um comentário")
    public ResponseEntity<MessageResponse> deleteComment(@PathVariable Long commentId,
                                                        @AuthenticationPrincipal UserDetails actor) {
        adminService.deleteComment(commentId, actor.getUsername());
        return ResponseEntity.ok(new MessageResponse("Comentário removido"));
    }

    @PostMapping("/users/{userId}/promote")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Promove usuário a admin")
    public ResponseEntity<MessageResponse> promote(@PathVariable Long userId,
                                                  @AuthenticationPrincipal UserDetails actor) {
        adminService.promoteToAdmin(userId, actor.getUsername());
        return ResponseEntity.ok(new MessageResponse("Usuário promovido a admin"));
    }

    @PostMapping("/users/{userId}/demote")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Remove privilégios de admin")
    public ResponseEntity<MessageResponse> demote(@PathVariable Long userId,
                                                 @AuthenticationPrincipal UserDetails actor) {
        adminService.demoteFromAdmin(userId, actor.getUsername());
        return ResponseEntity.ok(new MessageResponse("Usuário removido de admin"));
    }

    @PostMapping("/users/{userId}/ban")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Bane um usuário")
    public ResponseEntity<MessageResponse> ban(@PathVariable Long userId,
                                              @AuthenticationPrincipal UserDetails actor) {
        adminService.banUser(userId, actor.getUsername());
        return ResponseEntity.ok(new MessageResponse("Usuário banido"));
    }

    @PostMapping("/users/{userId}/unban")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Desbane um usuário")
    public ResponseEntity<MessageResponse> unban(@PathVariable Long userId,
                                                @AuthenticationPrincipal UserDetails actor) {
        adminService.unbanUser(userId, actor.getUsername());
        return ResponseEntity.ok(new MessageResponse("Usuário desbanido"));
    }

    @GetMapping("/audits")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Lista auditorias de ações de admin")
    public ResponseEntity<?> listAudits(@RequestParam(defaultValue = "0") int page,
                                        @RequestParam(defaultValue = "20") int size,
                                        @AuthenticationPrincipal UserDetails actor,
                                        com.soulsurf.backend.services.AdminAuditService auditService) {
        return ResponseEntity.ok(auditService.list(page, size));
    }

    @GetMapping("/metrics")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Métricas administrativas gerais")
    public ResponseEntity<?> metrics(com.soulsurf.backend.services.AdminMetricsService metricsService) {
        return ResponseEntity.ok(metricsService.getMetrics());
    }

    @GetMapping("/metrics/period")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Métricas por período (posts, comentários, usuários ativos)")
    public ResponseEntity<?> metricsByPeriod(@RequestParam String start,
                                             @RequestParam String end,
                                             com.soulsurf.backend.services.AdminMetricsService metricsService) {
        java.time.LocalDateTime s = java.time.LocalDateTime.parse(start);
        java.time.LocalDateTime e = java.time.LocalDateTime.parse(end);
        return ResponseEntity.ok(metricsService.getPeriodMetrics(s, e));
    }

    @GetMapping("/metrics/top-authors")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Top autores por número de posts no período")
    public ResponseEntity<?> topAuthors(@RequestParam String start,
                                        @RequestParam String end,
                                        @RequestParam(defaultValue = "10") int limit,
                                        com.soulsurf.backend.services.AdminMetricsService metricsService) {
        java.time.LocalDateTime s = java.time.LocalDateTime.parse(start);
        java.time.LocalDateTime e = java.time.LocalDateTime.parse(end);
        return ResponseEntity.ok(metricsService.getTopAuthors(s, e, limit));
    }

    @GetMapping("/metrics/by-beach")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Distribuição de posts por praia no período")
    public ResponseEntity<?> postsByBeach(@RequestParam String start,
                                          @RequestParam String end,
                                          com.soulsurf.backend.services.AdminMetricsService metricsService) {
        java.time.LocalDateTime s = java.time.LocalDateTime.parse(start);
        java.time.LocalDateTime e = java.time.LocalDateTime.parse(end);
        return ResponseEntity.ok(metricsService.getPostsByBeach(s, e));
    }
}


