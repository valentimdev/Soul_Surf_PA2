package com.soulsurf.backend.modules.post.controller;

import com.soulsurf.backend.modules.post.service.LikeService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/posts/{postId}/likes")
public class LikeController {

    private final LikeService likeService;

    public LikeController(LikeService likeService) {
        this.likeService = likeService;
    }

    @Operation(summary = "Dar ou remover like em um post", description = "Alterna o like do usuário autenticado no post especificado. Requer autenticação JWT.", security = @SecurityRequirement(name = "bearerAuth"))
    @ApiResponse(responseCode = "200", description = "Like alternado com sucesso")
    @ApiResponse(responseCode = "404", description = "Post não encontrado")
    @PostMapping
    public ResponseEntity<Map<String, Object>> toggleLike(
            @Parameter(description = "ID do post") @PathVariable Long postId,
            @AuthenticationPrincipal UserDetails userDetails) {
        boolean isLiked = likeService.toggleLike(postId, userDetails.getUsername());
        long likesCount = likeService.countLikes(postId);

        Map<String, Object> response = Map.of(
                "liked", isLiked,
                "likesCount", likesCount,
                "message", isLiked ? "Post curtido com sucesso" : "Like removido com sucesso");

        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Contar likes de um post", description = "Retorna a quantidade de likes de um post específico.")
    @ApiResponse(responseCode = "200", description = "Contagem de likes retornada com sucesso")
    @GetMapping("/count")
    public ResponseEntity<Map<String, Long>> getLikesCount(
            @Parameter(description = "ID do post") @PathVariable Long postId) {
        long count = likeService.countLikes(postId);
        return ResponseEntity.ok(Map.of("count", count));
    }

    @Operation(summary = "Verificar se usuário curtiu o post", description = "Verifica se o usuário autenticado curtiu o post. Requer autenticação JWT.", security = @SecurityRequirement(name = "bearerAuth"))
    @ApiResponse(responseCode = "200", description = "Status do like retornado com sucesso")
    @GetMapping("/status")
    public ResponseEntity<Map<String, Boolean>> getLikeStatus(
            @Parameter(description = "ID do post") @PathVariable Long postId,
            @AuthenticationPrincipal UserDetails userDetails) {
        boolean hasLiked = likeService.hasUserLiked(postId, userDetails.getUsername());
        return ResponseEntity.ok(Map.of("liked", hasLiked));
    }
}

