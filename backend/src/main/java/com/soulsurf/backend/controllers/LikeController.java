package com.soulsurf.backend.controllers;

import com.soulsurf.backend.dto.MessageResponse;
import com.soulsurf.backend.services.LikeService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@CrossOrigin(origins = "*", maxAge = 3600)
@RestController
@RequestMapping("/api/posts/{postId}/likes")
public class LikeController {

    private final LikeService likeService;

    public LikeController(LikeService likeService) {
        this.likeService = likeService;
    }

    @Operation(
            summary = "Dar ou remover like em um post",
            description = "Alterna o like do usuário autenticado no post especificado. Requer autenticação JWT.",
            security = @SecurityRequirement(name = "bearerAuth")
    )
    @ApiResponse(responseCode = "200", description = "Like alternado com sucesso")
    @ApiResponse(responseCode = "404", description = "Post não encontrado")
    @PostMapping
    public ResponseEntity<?> toggleLike(
            @Parameter(description = "ID do post") @PathVariable Long postId,
            @AuthenticationPrincipal UserDetails userDetails) {
        try {
            boolean isLiked = likeService.toggleLike(postId, userDetails.getUsername());
            long likesCount = likeService.countLikes(postId);

            Map<String, Object> response = new HashMap<>();
            response.put("liked", isLiked);
            response.put("likesCount", likesCount);
            response.put("message", isLiked ? "Post curtido com sucesso" : "Like removido com sucesso");

            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest()
                    .body(new MessageResponse("Erro ao alternar like: " + e.getMessage()));
        }
    }

    @Operation(
            summary = "Contar likes de um post",
            description = "Retorna a quantidade de likes de um post específico."
    )
    @ApiResponse(responseCode = "200", description = "Contagem de likes retornada com sucesso")
    @GetMapping("/count")
    public ResponseEntity<?> getLikesCount(
            @Parameter(description = "ID do post") @PathVariable Long postId) {
        try {
            long count = likeService.countLikes(postId);
            Map<String, Long> response = new HashMap<>();
            response.put("count", count);
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest()
                    .body(new MessageResponse("Erro ao contar likes: " + e.getMessage()));
        }
    }

    @Operation(
            summary = "Verificar se usuário curtiu o post",
            description = "Verifica se o usuário autenticado curtiu o post. Requer autenticação JWT.",
            security = @SecurityRequirement(name = "bearerAuth")
    )
    @ApiResponse(responseCode = "200", description = "Status do like retornado com sucesso")
    @GetMapping("/status")
    public ResponseEntity<?> getLikeStatus(
            @Parameter(description = "ID do post") @PathVariable Long postId,
            @AuthenticationPrincipal UserDetails userDetails) {
        try {
            boolean hasLiked = likeService.hasUserLiked(postId, userDetails.getUsername());
            Map<String, Boolean> response = new HashMap<>();
            response.put("liked", hasLiked);
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest()
                    .body(new MessageResponse("Erro ao verificar like: " + e.getMessage()));
        }
    }
}

