package com.soulsurf.backend.controllers;

import com.soulsurf.backend.dto.BeachDTO;
import com.soulsurf.backend.dto.MessageResponse;
import com.soulsurf.backend.dto.PostDTO;
import com.soulsurf.backend.services.BeachService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@CrossOrigin(origins = "*", maxAge = 3600)
@RestController
@RequestMapping("/api/beaches")
@Tag(name = "3. Beaches", description = "Endpoints para gerenciar praias.")
public class BeachController {

    private final BeachService beachService;

    public BeachController(BeachService beachService) {
        this.beachService = beachService;
    }

    @Operation(summary = "Lista todas as praias", description = "Retorna uma lista de todas as praias cadastradas.")
    @ApiResponse(responseCode = "200", description = "Praias listadas com sucesso")
    @GetMapping("/")
    public ResponseEntity<List<BeachDTO>> getAllBeaches() {
        List<BeachDTO> beaches = beachService.getAllBeaches();
        return ResponseEntity.ok(beaches);
    }

    @Operation(summary = "Busca uma praia pelo ID", description = "Retorna os detalhes de uma única praia.")
    @ApiResponse(responseCode = "200", description = "Praia encontrada")
    @ApiResponse(responseCode = "404", description = "Praia não encontrada")
    @GetMapping("/{id}")
    public ResponseEntity<BeachDTO> getBeachById(@Parameter(description = "ID da praia") @PathVariable Long id) {
        return beachService.getBeachById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @Operation(summary = "Cria uma nova praia", description = "Adiciona uma nova praia ao sistema.", security = @SecurityRequirement(name = "bearerAuth"))
    @ApiResponse(responseCode = "201", description = "Praia criada com sucesso")
    @ApiResponse(responseCode = "400", description = "Erro ao criar a praia")
    @PostMapping("/")
    public ResponseEntity<?> createBeach(
            @Parameter(description = "Nome da praia") @RequestParam("nome") String nome,
            @Parameter(description = "Descrição da praia") @RequestParam("descricao") String descricao,
            @Parameter(description = "Localização da praia") @RequestParam("localizacao") String localizacao,
            @Parameter(description = "Nível de experiência") @RequestParam("nivelExperiencia") String nivelExperiencia,
            @Parameter(description = "Foto da praia (opcional)") @RequestParam(value = "foto", required = false) MultipartFile foto) {
        try {
            beachService.createBeach(nome, descricao, localizacao, nivelExperiencia, foto);
            return ResponseEntity
                    .status(HttpStatus.CREATED)
                    .body(new MessageResponse("Praia criada com sucesso!"));
        } catch (Exception e) {
            return ResponseEntity
                    .badRequest()
                    .body(new MessageResponse("Erro ao criar a praia: " + e.getMessage()));
        }
    }

    @Operation(summary = "Busca posts de uma praia", description = "Retorna todos os posts públicos associados a uma praia específica.")
    @ApiResponse(responseCode = "200", description = "Posts encontrados")
    @ApiResponse(responseCode = "404", description = "Praia não encontrada")
    @GetMapping("/{id}/posts")
    public ResponseEntity<List<PostDTO>> getBeachPosts(
            @Parameter(description = "ID da praia") @PathVariable Long id,
            @Parameter(description = "Número da página (começa em 0)") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Tamanho da página") @RequestParam(defaultValue = "20") int size,
            @AuthenticationPrincipal UserDetails userDetails) {
        try {
            String userEmail = userDetails != null ? userDetails.getUsername() : null;
            List<PostDTO> posts = beachService.getBeachPosts(id, page, size, userEmail);
            return ResponseEntity.ok(posts);
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @Operation(summary = "Lista todos os posts de uma praia (Admin)", description = "Retorna todos os posts de uma praia, incluindo privados. Requer autenticação de admin.", security = @SecurityRequirement(name = "bearerAuth"))
    @ApiResponse(responseCode = "200", description = "Posts listados com sucesso")
    @ApiResponse(responseCode = "403", description = "Acesso negado")
    @ApiResponse(responseCode = "404", description = "Praia não encontrada")
    @GetMapping("/{id}/all-posts")
    public ResponseEntity<List<PostDTO>> getAllBeachPosts(@Parameter(description = "ID da praia") @PathVariable Long id,
                                                        @AuthenticationPrincipal UserDetails userDetails) {
        try {
            List<PostDTO> posts = beachService.getAllBeachPosts(id, userDetails.getUsername());
            return ResponseEntity.ok(posts);
        } catch (SecurityException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }
}