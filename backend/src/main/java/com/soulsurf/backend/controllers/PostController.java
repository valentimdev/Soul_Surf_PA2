package com.soulsurf.backend.controllers;

import com.soulsurf.backend.dto.MessageResponse;
import com.soulsurf.backend.dto.PostDTO;
import com.soulsurf.backend.services.PostService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@CrossOrigin(origins = "*", maxAge = 3600)
@RestController
@RequestMapping("/api/posts")
@Tag(name = "2. Posts", description = "Endpoints para gerenciar posts (registros).")
public class PostController {

    private final PostService postService;

    public PostController(PostService postService) {
        this.postService = postService;
    }

    @Operation(summary = "Cria um novo post", description = "Cria um novo registro associado ao usuário autenticado. Requer autenticação JWT.", security = @SecurityRequirement(name = "bearerAuth"))
    @ApiResponse(responseCode = "201", description = "Post criado com sucesso")
    @ApiResponse(responseCode = "400", description = "Erro ao criar o post")
    @PostMapping("/")
    public ResponseEntity<?> createPost(@Parameter(description = "Visibilidade do post") @RequestParam("publico") boolean publico,
                                        @Parameter(description = "Descrição do post") @RequestParam("descricao") String descricao,
                                        @Parameter(description = "Arquivo de imagem para o post (opcional)") @RequestParam(value = "foto", required = false) MultipartFile foto,
                                        @AuthenticationPrincipal UserDetails userDetails) {
        try {
            String userEmail = userDetails.getUsername();
            postService.createPost(publico, descricao, foto, userEmail);
            return ResponseEntity
                    .status(HttpStatus.CREATED)
                    .body(new MessageResponse("Post criado com sucesso!"));
        } catch (Exception e) {
            return ResponseEntity
                    .badRequest()
                    .body(new MessageResponse("Erro ao criar o post: " + e.getMessage()));
        }
    }

    @Operation(summary = "Lista todos os posts públicos", description = "Retorna uma lista de todos os posts marcados como públicos, ideal para o feed principal.")
    @ApiResponse(responseCode = "200", description = "Posts listados com sucesso")
    @GetMapping("/home")
    public ResponseEntity<List<PostDTO>> getPublicFeed() {
        List<PostDTO> posts = postService.getPublicFeed();
        return ResponseEntity.ok(posts);
    }

    @Operation(summary = "Busca um post pelo ID", description = "Retorna os detalhes de um único post. Se o post for privado, só será retornado para o seu dono.")
    @ApiResponse(responseCode = "200", description = "Post encontrado")
    @ApiResponse(responseCode = "404", description = "Post não encontrado ou acesso negado")
    @GetMapping("/{id}")
    public ResponseEntity<PostDTO> getPostById(@Parameter(description = "ID do post a ser buscado") @PathVariable Long id,
                                               @Parameter(hidden = true) @AuthenticationPrincipal UserDetails userDetails) { 
        
        String requesterEmail = (userDetails != null) ? userDetails.getUsername() : null;

        return postService.getPostById(id, requesterEmail)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @Operation(summary = "Lista posts de um usuário", description = "Retorna uma lista de todos os posts de um usuário específico pelo e-mail.")
    @ApiResponse(responseCode = "200", description = "Posts listados com sucesso")
    @ApiResponse(responseCode = "404", description = "Usuário não encontrado")
    @GetMapping("/user/{userEmail}")
    public ResponseEntity<List<PostDTO>> getPostsByUser(@Parameter(description = "E-mail do usuário para buscar os posts") @PathVariable String userEmail) {
        try {
            List<PostDTO> posts = postService.getPostsByUserEmail(userEmail);
            return ResponseEntity.ok(posts);
        } catch (UsernameNotFoundException e) {
            return ResponseEntity.notFound().build();
        }
    }
}