package com.soulsurf.backend.modules.beach.controller;

import com.soulsurf.backend.modules.beach.dto.BeachDTO;
import com.soulsurf.backend.modules.post.dto.PostDTO;
import com.soulsurf.backend.modules.beach.entity.Beach;
import com.soulsurf.backend.modules.beach.mapper.BeachMapper;
import com.soulsurf.backend.modules.beach.service.BeachService;
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

@RestController
@RequestMapping("/api/beaches")
@Tag(name = "3. Beaches", description = "Endpoints para gerenciar praias.")
public class BeachController {

    private final BeachService beachService;
    private final BeachMapper beachMapper;

    public BeachController(BeachService beachService, BeachMapper beachMapper) {
        this.beachService = beachService;
        this.beachMapper = beachMapper;
    }

    @Operation(summary = "Lista todas as praias", description = "Retorna uma lista de todas as praias cadastradas.")
    @ApiResponse(responseCode = "200", description = "Praias listadas com sucesso")
    @GetMapping
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
    public ResponseEntity<BeachDTO> createBeach(
            @RequestParam("nome") String nome,
            @RequestParam("descricao") String descricao,
            @RequestParam("localizacao") String localizacao,
            @RequestParam("nivelExperiencia") String nivelExperiencia,
            @RequestParam(value = "foto", required = false) MultipartFile foto) {
        Beach beach = beachService.createBeach(nome, descricao, localizacao, nivelExperiencia, foto);
        BeachDTO dto = beachMapper.toDto(beach);
        return ResponseEntity.status(HttpStatus.CREATED).body(dto);
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
        String userEmail = userDetails != null ? userDetails.getUsername() : null;
        List<PostDTO> posts = beachService.getBeachPosts(id, page, size, userEmail);
        return ResponseEntity.ok(posts);
    }

    @Operation(summary = "Lista todos os posts de uma praia (Admin)", description = "Retorna todos os posts de uma praia, incluindo privados. Requer autenticação de admin.", security = @SecurityRequirement(name = "bearerAuth"))
    @ApiResponse(responseCode = "200", description = "Posts listados com sucesso")
    @ApiResponse(responseCode = "403", description = "Acesso negado")
    @ApiResponse(responseCode = "404", description = "Praia não encontrada")
    @GetMapping("/{id}/all-posts")
    public ResponseEntity<List<PostDTO>> getAllBeachPosts(@Parameter(description = "ID da praia") @PathVariable Long id,
            @AuthenticationPrincipal UserDetails userDetails) {
        List<PostDTO> posts = beachService.getAllBeachPosts(id, userDetails.getUsername());
        return ResponseEntity.ok(posts);
    }
}

