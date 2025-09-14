package com.soulsurf.backend.services;

import com.soulsurf.backend.entities.Post;
import com.soulsurf.backend.entities.User;
import com.soulsurf.backend.repository.PostRepository;
import com.soulsurf.backend.repository.UserRepository;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Optional;

@Service
public class PostService {

    private final PostRepository postRepository;
    private final UserRepository userRepository;
    private final Optional<BlobStorageService> blobStorageService;

    public PostService(PostRepository postRepository, UserRepository userRepository, Optional<BlobStorageService> blobStorageService) {
        this.postRepository = postRepository;
        this.userRepository = userRepository;
        this.blobStorageService = blobStorageService;
    }

    public Post createPost(String titulo, String descricao, MultipartFile foto, String userEmail) throws IOException {
        User usuario = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new UsernameNotFoundException("Usuário não encontrado com o e-mail: " + userEmail));

        String urlDaFoto = null;
        if (blobStorageService.isPresent() && foto != null && !foto.isEmpty()) {
            urlDaFoto = blobStorageService.get().uploadFile(foto);
        }

        Post novoPost = new Post();
        novoPost.setTitulo(titulo);
        novoPost.setDescricao(descricao);
        novoPost.setUsuario(usuario);
        novoPost.setCaminhoFoto(urlDaFoto);

        return postRepository.save(novoPost);
    }
}