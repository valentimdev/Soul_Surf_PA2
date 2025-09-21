package com.soulsurf.backend.controllers;

import com.soulsurf.backend.services.BlobStorageService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/files")
@Tag(name = "4. Ficheiros", description = "Endpoints para upload e listagem de ficheiros.")
public class FileController {

    private final Optional<BlobStorageService> blobStorageService;

    public FileController(Optional<BlobStorageService> blobStorageService) {
        this.blobStorageService = blobStorageService;
    }

    @Operation(summary = "Faz o upload de um ficheiro",
            description = "Envia um ficheiro para o armazenamento na nuvem (Azure Blob Storage). Requer autenticação JWT e o 'Content-Type' deve ser 'multipart/form-data'.",
            security = @SecurityRequirement(name = "bearerAuth"))
    @ApiResponse(responseCode = "200", description = "Ficheiro enviado com sucesso, retorna a URL pública")
    @ApiResponse(responseCode = "500", description = "Erro no upload ou serviço de armazenamento não está ativo")
    @PostMapping(value = "/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<?> uploadFile(@RequestParam("file") MultipartFile file) {
        if (blobStorageService.isEmpty()) {
            return ResponseEntity.status(500).body("Serviço de upload de ficheiros não está ativo.");
        }

        try {
            String url = blobStorageService.get().uploadFile(file);
            return ResponseEntity.ok().body("Ficheiro enviado com sucesso: " + url);
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Erro no upload: " + e.getMessage());
        }
    }

    @Operation(summary = "Lista todos os ficheiros", description = "Retorna uma lista de URLs para todos os ficheiros armazenados. Requer autenticação JWT.", security = @SecurityRequirement(name = "bearerAuth"))
    @ApiResponse(responseCode = "200", description = "Lista de URLs retornada com sucesso")
    @GetMapping("/list")
    public ResponseEntity<List<String>> listFiles() {
        if (blobStorageService.isEmpty()) {
            return ResponseEntity.ok(Collections.emptyList());
        }

        List<String> files = blobStorageService.get().listFiles();
        return ResponseEntity.ok(files);
    }
}