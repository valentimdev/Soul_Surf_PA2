package com.soulsurf.backend.controllers;

import com.soulsurf.backend.dto.CommentDTO;
import com.soulsurf.backend.dto.MessageResponse;
import com.soulsurf.backend.services.CommentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@CrossOrigin(origins = "*", maxAge = 3600)
@RestController
@RequestMapping("/api/posts/{postId}/comments")
public class CommentController {

    private final CommentService commentService;

    public CommentController(CommentService commentService) {
        this.commentService = commentService;
    }

    @Operation(summary = "Adiciona um comentário", description = "Adiciona um novo comentário a um post. Requer autenticação.", security = @SecurityRequirement(name = "bearerAuth"))
    @ApiResponse(responseCode = "200", description = "Comentário adicionado com sucesso")
    @ApiResponse(responseCode = "404", description = "Post não encontrado")
    @PostMapping("/")
    public ResponseEntity<?> addComment(
            @Parameter(description = "ID do post") @PathVariable Long postId,
            @Parameter(description = "Texto do comentário") @RequestParam("texto") String texto,
            @AuthenticationPrincipal UserDetails userDetails) {
        try {
            CommentDTO comment = commentService.createComment(postId, texto, userDetails.getUsername());
            return ResponseEntity.ok(comment);
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(new MessageResponse("Erro ao adicionar comentário: " + e.getMessage()));
        }
    }

    @Operation(summary = "Lista comentários de um post", description = "Retorna todos os comentários de um post específico.")
    @ApiResponse(responseCode = "200", description = "Comentários listados com sucesso")
    @ApiResponse(responseCode = "404", description = "Post não encontrado")
    @GetMapping("/")
    public ResponseEntity<?> getPostComments(
            @Parameter(description = "ID do post") @PathVariable Long postId) {
        try {
            List<CommentDTO> comments = commentService.getPostComments(postId);
            return ResponseEntity.ok(comments);
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(new MessageResponse("Erro ao buscar comentários: " + e.getMessage()));
        }
    }

    @Operation(summary = "Atualiza um comentário", description = "Atualiza o texto de um comentário existente. Requer autenticação.", security = @SecurityRequirement(name = "bearerAuth"))
    @ApiResponse(responseCode = "200", description = "Comentário atualizado com sucesso")
    @ApiResponse(responseCode = "404", description = "Comentário não encontrado")
    @PutMapping("/{commentId}")
    public ResponseEntity<?> updateComment(
            @Parameter(description = "ID do post") @PathVariable Long postId,
            @Parameter(description = "ID do comentário") @PathVariable Long commentId,
            @Parameter(description = "Novo texto do comentário") @RequestParam("texto") String texto,
            @AuthenticationPrincipal UserDetails userDetails) {
        try {
            CommentDTO updatedComment = commentService.updateComment(postId, commentId, texto, userDetails.getUsername());
            return ResponseEntity.ok(updatedComment);
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(new MessageResponse("Erro ao atualizar comentário: " + e.getMessage()));
        }
    }

    @Operation(summary = "Remove um comentário", description = "Remove um comentário existente. Requer autenticação.", security = @SecurityRequirement(name = "bearerAuth"))
    @ApiResponse(responseCode = "200", description = "Comentário removido com sucesso")
    @ApiResponse(responseCode = "404", description = "Comentário não encontrado")
    @DeleteMapping("/{commentId}")
    public ResponseEntity<?> deleteComment(
            @Parameter(description = "ID do post") @PathVariable Long postId,
            @Parameter(description = "ID do comentário") @PathVariable Long commentId,
            @AuthenticationPrincipal UserDetails userDetails) {
        try {
            commentService.deleteComment(postId, commentId, userDetails.getUsername());
            return ResponseEntity.ok(new MessageResponse("Comentário removido com sucesso"));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(new MessageResponse("Erro ao remover comentário: " + e.getMessage()));
        }
    }
}