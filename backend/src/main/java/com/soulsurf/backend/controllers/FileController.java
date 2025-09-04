package com.example.demo.controller;

import com.example.demo.service.BlobStorageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/files")
public class FileController {

    @Autowired
    private BlobStorageService blobStorageService;

    @PostMapping("/upload")
    public ResponseEntity<?> uploadFile(@RequestParam("file") MultipartFile file) {
        try {
            String url = blobStorageService.uploadFile(file);
            return ResponseEntity.ok().body("Arquivo enviado com sucesso: " + url);
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Erro no upload: " + e.getMessage());
        }
    }
    @GetMapping("/list")
    public ResponseEntity<List<String>> listFiles() {
        List<String> files = blobStorageService.listFiles();
        return ResponseEntity.ok(files);
    }
}
