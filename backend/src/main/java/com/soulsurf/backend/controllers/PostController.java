package com.soulsurf.backend.controllers;

import com.soulsurf.backend.dto.MessageResponse;
import com.soulsurf.backend.dto.PostDTO;
import com.soulsurf.backend.services.PostService;
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
public class PostController {

    private final PostService postService;

    public PostController(PostService postService) {
        this.postService = postService;
    }

    @PostMapping
    public ResponseEntity<?> createPost(@RequestParam("titulo") String titulo,
                                        @RequestParam("descricao") String descricao,
                                        @RequestParam(value = "foto", required = false) MultipartFile foto,
                                        @AuthenticationPrincipal UserDetails userDetails) {
        try {
            String userEmail = userDetails.getUsername();
            postService.createPost(titulo, descricao, foto, userEmail);
            return ResponseEntity
                    .status(HttpStatus.CREATED)
                    .body(new MessageResponse("Post criado com sucesso!"));
        } catch (Exception e) {
            return ResponseEntity
                    .badRequest()
                    .body(new MessageResponse("Erro ao criar o post: " + e.getMessage()));
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<PostDTO> getPostById(@PathVariable Long id) {
        return postService.getPostById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/user/{userEmail}")
    public ResponseEntity<List<PostDTO>> getPostsByUser(@PathVariable String userEmail) {
        try {
            List<PostDTO> posts = postService.getPostsByUserEmail(userEmail);
            return ResponseEntity.ok(posts);
        } catch (UsernameNotFoundException e) {
            return ResponseEntity.notFound().build();
        }
    }
}