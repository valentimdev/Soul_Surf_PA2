package com.soulsurf.backend.modules.comment.controller;

import com.soulsurf.backend.modules.comment.dto.CommentDTO;
import com.soulsurf.backend.modules.chat.dto.MessageResponse;
import com.soulsurf.backend.modules.comment.service.CommentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/posts/{postId}/comments")
public class CommentController {

    private final CommentService commentService;

    public CommentController(CommentService commentService) {
        this.commentService = commentService;
    }

    @Operation(summary = "Adiciona um comentário", description = "Adiciona um novo comentário a um post ou responde a um comentário existente. Requer autenticação.", security = @SecurityRequirement(name = "bearerAuth"))
    @ApiResponse(responseCode = "200", description = "Comentário adicionado com sucesso")
    @ApiResponse(responseCode = "404", description = "Post ou comentário pai não encontrado")
    @PostMapping("/")
    public ResponseEntity<CommentDTO> addComment(
            @Parameter(description = "ID do post") @PathVariable Long postId,
            @Parameter(description = "ID do comentário pai (opcional, para respostas)") @RequestParam(value = "parentId", required = false) Long parentId,
            @Parameter(description = "Texto do comentário") @RequestParam("texto") String texto,
            @AuthenticationPrincipal UserDetails userDetails) {
        CommentDTO comment = commentService.createComment(postId, parentId, texto, userDetails.getUsername());
        return ResponseEntity.ok(comment);
    }

    @Operation(summary = "Lista comentários de um post", description = "Retorna todos os comentários de um post específico, aninhados em threads.")
    @ApiResponse(responseCode = "200", description = "Comentários listados com sucesso")
    @ApiResponse(responseCode = "404", description = "Post não encontrado")
    @GetMapping("/")
    public ResponseEntity<List<CommentDTO>> getPostComments(
            @Parameter(description = "ID do post") @PathVariable Long postId) {
        List<CommentDTO> comments = commentService.getPostComments(postId);
        return ResponseEntity.ok(comments);
    }

    @Operation(summary = "Atualiza um comentário", description = "Atualiza o texto de um comentário existente. Requer autenticação.", security = @SecurityRequirement(name = "bearerAuth"))
    @ApiResponse(responseCode = "200", description = "Comentário atualizado com sucesso")
    @ApiResponse(responseCode = "404", description = "Comentário não encontrado")
    @PutMapping("/{commentId}")
    public ResponseEntity<CommentDTO> updateComment(
            @Parameter(description = "ID do post") @PathVariable Long postId,
            @Parameter(description = "ID do comentário") @PathVariable Long commentId,
            @Parameter(description = "Novo texto do comentário") @RequestParam("texto") String texto,
            @AuthenticationPrincipal UserDetails userDetails) {
        CommentDTO updatedComment = commentService.updateComment(postId, commentId, texto, userDetails.getUsername());
        return ResponseEntity.ok(updatedComment);
    }

    @Operation(summary = "Remove um comentário", description = "Remove um comentário existente. Requer autenticação.", security = @SecurityRequirement(name = "bearerAuth"))
    @ApiResponse(responseCode = "200", description = "Comentário removido com sucesso")
    @ApiResponse(responseCode = "404", description = "Comentário não encontrado")
    @DeleteMapping("/{commentId}")
    public ResponseEntity<MessageResponse> deleteComment(
            @Parameter(description = "ID do post") @PathVariable Long postId,
            @Parameter(description = "ID do comentário") @PathVariable Long commentId,
            @AuthenticationPrincipal UserDetails userDetails) {
        commentService.deleteComment(postId, commentId, userDetails.getUsername());
        return ResponseEntity.ok(new MessageResponse("Comentário removido com sucesso"));
    }
}
