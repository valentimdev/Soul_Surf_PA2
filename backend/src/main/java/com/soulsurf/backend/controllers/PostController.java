package com.soulsurf.backend.controllers;

import com.soulsurf.backend.dto.CreatePostRequest;
import com.soulsurf.backend.dto.MessageResponse;
import com.soulsurf.backend.dto.PostDTO;
import com.soulsurf.backend.entities.User;
import com.soulsurf.backend.repository.UserRepository;
import com.soulsurf.backend.services.PostService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
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
    @Autowired
    private UserRepository userRepository;

    public PostController(PostService postService) {
        this.postService = postService;
    }

    @Operation(
            summary = "Cria um novo post",
            description = "Cria um novo registro associado ao usuário autenticado. Requer autenticação JWT.",
            security = @SecurityRequirement(name = "bearerAuth")
    )
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Post criado com sucesso"),
            @ApiResponse(responseCode = "400", description = "Erro ao criar o post")
    })
    @PostMapping
    public ResponseEntity<PostDTO> createPost(
            @RequestParam("publico") boolean publico,
            @RequestParam("descricao") String descricao,
            @RequestParam(value = "foto", required = false) MultipartFile foto,
            @RequestParam(value = "beachId", required = false) Long beachId,
            @AuthenticationPrincipal UserDetails userDetails) {

        try {
            CreatePostRequest request = new CreatePostRequest();
            request.setDescricao(descricao);
            request.setPublico(publico);
            request.setBeachId(beachId);

            PostDTO createdPost = postService.createPost(request, foto, userDetails.getUsername());
            return ResponseEntity.status(HttpStatus.CREATED).body(createdPost);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @Operation(
            summary = "Lista todos os posts públicos (Feed Principal)",
            description = "Retorna uma lista de todos os posts marcados como públicos, ideal para o feed principal."
    )
    @ApiResponse(responseCode = "200", description = "Posts listados com sucesso")
    @GetMapping("/home")
    public ResponseEntity<List<PostDTO>> getPublicFeed() {
        return ResponseEntity.ok(postService.getPublicFeed());
    }

    @Operation(
            summary = "Lista os posts dos usuários que você segue (Feed 'Seguindo')",
            description = "Retorna um feed com os posts das pessoas que o usuário autenticado segue. Requer autenticação JWT.",
            security = @SecurityRequirement(name = "bearerAuth")
    )
    @ApiResponse(responseCode = "200", description = "Feed 'seguindo' listado com sucesso")
    @GetMapping("/following")
    public ResponseEntity<List<PostDTO>> getFollowingFeed(
            @AuthenticationPrincipal UserDetails userDetails) {
        List<PostDTO> posts = postService.getFollowingPosts(userDetails.getUsername());
        return ResponseEntity.ok(posts);
    }

    @Operation(
            summary = "Busca um post pelo ID",
            description = "Retorna os detalhes de um único post. Se for privado, só retorna para o dono."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Post encontrado"),
            @ApiResponse(responseCode = "404", description = "Post não encontrado ou acesso negado")
    })
    @GetMapping("/{id}")
    public ResponseEntity<PostDTO> getPostById(
            @Parameter(description = "ID do post a ser buscado") @PathVariable Long id,
            @Parameter(hidden = true) @AuthenticationPrincipal UserDetails userDetails) {

        String requesterEmail = (userDetails != null) ? userDetails.getUsername() : null;

        return postService.getPostById(id, requesterEmail)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @Operation(
            summary = "Lista posts de um usuário",
            description = "Retorna uma lista de todos os posts de um usuário específico pelo e-mail."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Posts listados com sucesso"),
            @ApiResponse(responseCode = "404", description = "Usuário não encontrado")
    })
    @GetMapping("/user/{userEmail}")
    public ResponseEntity<List<PostDTO>> getPostsByUser(
            @Parameter(description = "E-mail do usuário") @PathVariable String userEmail) {
        try {
            return ResponseEntity.ok(postService.getPostsByUserEmail(userEmail));
        } catch (UsernameNotFoundException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @Operation(
            summary = "Edita um post existente",
            description = "Atualiza a descrição de um post existente. Apenas o dono pode editar. Requer autenticação JWT.",
            security = @SecurityRequirement(name = "bearerAuth")
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Post atualizado com sucesso"),
            @ApiResponse(responseCode = "403", description = "Acesso negado - não é dono do post"),
            @ApiResponse(responseCode = "404", description = "Post não encontrado")
    })
    @PutMapping("/{id}")
    public ResponseEntity<MessageResponse> updatePost(
            @Parameter(description = "ID do post a ser editado") @PathVariable Long id,
            @Parameter(description = "Nova descrição do post") @RequestParam("descricao") String descricao,
            @AuthenticationPrincipal UserDetails userDetails) {

        try {
            postService.updatePost(id, descricao, userDetails.getUsername());
            return ResponseEntity.ok(new MessageResponse("Post atualizado com sucesso!"));
        } catch (SecurityException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(new MessageResponse("Você não tem permissão para editar este post"));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(new MessageResponse("Erro ao atualizar o post: " + e.getMessage()));
        }
    }

    @Operation(
            summary = "Exclui um post existente",
            description = "Remove um post do banco de dados. Apenas o dono pode excluir. Requer autenticação JWT.",
            security = @SecurityRequirement(name = "bearerAuth")
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Post excluído com sucesso"),
            @ApiResponse(responseCode = "403", description = "Acesso negado - não é dono do post"),
            @ApiResponse(responseCode = "404", description = "Post não encontrado")
    })
    @DeleteMapping("/{id}")
    public ResponseEntity<MessageResponse> deletePost(
            @PathVariable Long id,
            @AuthenticationPrincipal UserDetails userDetails) {

        try {
            User user = userRepository.findByUsername(userDetails.getUsername())
                    .orElseThrow(() -> new IllegalArgumentException("Usuário não encontrado"));

            postService.deletePost(id, user);

            return ResponseEntity.ok(new MessageResponse("Post excluído com sucesso!"));
        } catch (SecurityException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(new MessageResponse("Você não tem permissão para excluir este post"));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new MessageResponse("Post ou usuário não encontrado"));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(new MessageResponse("Erro ao excluir o post: " + e.getMessage()));
        }
    }
}