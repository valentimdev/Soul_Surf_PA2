package com.soulsurf.backend.services;

import com.soulsurf.backend.dto.PostDTO;
import com.soulsurf.backend.dto.UserDTO;
import com.soulsurf.backend.entities.Post;
import com.soulsurf.backend.entities.User;
import com.soulsurf.backend.repository.PostRepository;
import com.soulsurf.backend.repository.UserRepository;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

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

    public void createPost(boolean publico, String descricao, MultipartFile foto, String userEmail) throws IOException {
        User usuario = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new UsernameNotFoundException("Usuário não encontrado com o e-mail: " + userEmail));

        String urlDaFoto = null;
        if (blobStorageService.isPresent() && foto != null && !foto.isEmpty()) {
            urlDaFoto = blobStorageService.get().uploadFile(foto);
        }

        Post novoPost = new Post();
        novoPost.setDescricao(descricao);
        novoPost.setUsuario(usuario);
        novoPost.setCaminhoFoto(urlDaFoto);
        novoPost.setPublico(publico);

        postRepository.save(novoPost);
    }

    // 
    public List<PostDTO> getPublicFeed() {
        return postRepository.findByPublicoIsTrueOrderByDataDesc().stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }


    public List<PostDTO> getPostsByUserEmail(String userEmail) {
        User usuario = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new UsernameNotFoundException("Usuário não encontrado com o e-mail: " + userEmail));

        List<Post> posts = postRepository.findByUsuarioOrderByDataDesc(usuario);

        return posts.stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    public Optional<PostDTO> getPostById(Long id, String requesterEmail) {
        Optional<Post> postOptional = postRepository.findById(id);
        
        if (postOptional.isEmpty()) {
            return Optional.empty();
        }

        Post post = postOptional.get();
        // verifica se é dono do post ou o post é publico
        if (post.isPublico() || post.getUsuario().getEmail().equals(requesterEmail)) {
            return Optional.of(convertToDto(post));
        }

        // se o nao for o dono do post e o post for privado retorna null
        return Optional.empty();
    }

    public void updatePost(Long id, boolean publico, String descricao, String userEmail) {
        Post post = postRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Post não encontrado"));
        if (!post.getUsuario().getEmail().equals(userEmail)) {
            throw new SecurityException("Usuário não tem permissão para editar este post");
        }
        post.setPublico(publico);
        post.setDescricao(descricao);

        postRepository.save(post);
    }

    private PostDTO convertToDto(Post post) {
        PostDTO postDTO = new PostDTO();
        postDTO.setId(post.getId());
        postDTO.setPublico(post.isPublico());
        postDTO.setDescricao(post.getDescricao());
        postDTO.setCaminhoFoto(post.getCaminhoFoto());
        postDTO.setData(post.getData());

        UserDTO userDTO = new UserDTO();
        userDTO.setId(post.getUsuario().getId());
        userDTO.setUsername(post.getUsuario().getUsername());
        userDTO.setEmail(post.getUsuario().getEmail());

        postDTO.setUsuario(userDTO); 

        return postDTO;
    }
}