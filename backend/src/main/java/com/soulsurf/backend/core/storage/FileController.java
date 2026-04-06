package com.soulsurf.backend.core.storage;

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
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

@RestController
@RequestMapping("/api/files")
@Tag(name = "4. Ficheiros", description = "Endpoints para upload e listagem de ficheiros.")
public class FileController {

    private static final long MAX_UPLOAD_SIZE_BYTES = 10L * 1024L * 1024L;
    private static final Set<String> ALLOWED_CONTENT_TYPES = Set.of(
            "image/jpeg",
            "image/png",
            "image/webp");

    private final Optional<OracleStorageService> blobStorageService;

    public FileController(Optional<OracleStorageService> blobStorageService) {
        this.blobStorageService = blobStorageService;
    }

    @Operation(
            summary = "Faz o upload de um ficheiro",
            description = "Envia um ficheiro para o armazenamento na nuvem (OCI Object Storage). Requer autenticacao JWT e o Content-Type deve ser multipart/form-data.",
            security = @SecurityRequirement(name = "bearerAuth"))
    @ApiResponse(responseCode = "200", description = "Ficheiro enviado com sucesso, retorna a URL publica")
    @ApiResponse(responseCode = "400", description = "Ficheiro invalido")
    @ApiResponse(responseCode = "503", description = "Servico de armazenamento indisponivel")
    @PostMapping(value = "/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<?> uploadFile(@RequestParam("file") MultipartFile file) {
        if (blobStorageService.isEmpty()) {
            return ResponseEntity.status(503).body(Map.of("message", "Servico de upload indisponivel."));
        }

        if (file == null || file.isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of("message", "Arquivo vazio."));
        }

        if (file.getSize() > MAX_UPLOAD_SIZE_BYTES) {
            return ResponseEntity.badRequest().body(Map.of("message", "Arquivo excede o limite de 10MB."));
        }

        String contentType = Optional.ofNullable(file.getContentType())
                .map(value -> value.toLowerCase(Locale.ROOT))
                .orElse("");
        if (!ALLOWED_CONTENT_TYPES.contains(contentType)) {
            return ResponseEntity.badRequest().body(Map.of("message", "Tipo de arquivo nao permitido."));
        }

        String originalFilename = Optional.ofNullable(file.getOriginalFilename()).orElse("");
        if (originalFilename.contains("..") || originalFilename.contains("/") || originalFilename.contains("\\")) {
            return ResponseEntity.badRequest().body(Map.of("message", "Nome de arquivo invalido."));
        }

        try {
            String url = blobStorageService.get().uploadFile(file);
            return ResponseEntity.ok(Map.of("url", url));
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of("message", "Erro no upload do ficheiro."));
        }
    }

    @Operation(
            summary = "Lista todos os ficheiros",
            description = "Retorna uma lista de URLs para todos os ficheiros armazenados. Requer autenticacao JWT.",
            security = @SecurityRequirement(name = "bearerAuth"))
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
