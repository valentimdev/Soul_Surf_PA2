package com.soulsurf.backend.controllers;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import com.soulsurf.backend.services.BlobStorageService;

@RestController
@RequestMapping("/api/files")
public class FileController {

    private final Optional<BlobStorageService> blobStorageService;

    public FileController(Optional<BlobStorageService> blobStorageService) {
        this.blobStorageService = blobStorageService;
    }

    @PostMapping("/upload")
    public ResponseEntity<?> uploadFile(@RequestParam("file") MultipartFile file) {
        if (!blobStorageService.isPresent()) {
            return ResponseEntity.ok().body("Serviço de upload de arquivos não está ativo.");
        }

        try {
            String url = blobStorageService.get().uploadFile(file);
            return ResponseEntity.ok().body("Arquivo enviado com sucesso: " + url);
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Erro no upload: " + e.getMessage());
        }
    }

    @GetMapping("/list")
    public ResponseEntity<List<String>> listFiles() {
        if (!blobStorageService.isPresent()) {
            return ResponseEntity.ok(Collections.emptyList());
        }

        List<String> files = blobStorageService.get().listFiles();
        return ResponseEntity.ok(files);
    }
}