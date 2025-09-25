package com.soulsurf.backend.services;

import com.azure.core.exception.ResourceNotFoundException;
import com.soulsurf.backend.dto.BeachDTO;
import com.soulsurf.backend.dto.CommentDTO;
import com.soulsurf.backend.dto.CreatePostRequest;
import com.soulsurf.backend.dto.PostDTO;
import com.soulsurf.backend.dto.UserDTO;
import com.soulsurf.backend.entities.Beach;
import com.soulsurf.backend.entities.Post;
import com.soulsurf.backend.entities.User;
import com.soulsurf.backend.repository.BeachRepository;
import com.soulsurf.backend.repository.PostRepository;
import com.soulsurf.backend.repository.UserRepository;

import jakarta.transaction.Transactional;

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
    private final BeachRepository beachRepository;
    private final Optional<BlobStorageService> blobStorageService;

    public PostService(PostRepository postRepository,
                       UserRepository userRepository,
                       BeachRepository beachRepository,
                       Optional<BlobStorageService> blobStorageService) {
        this.postRepository = postRepository;
        this.userRepository = userRepository;
        this.beachRepository = beachRepository;
        this.blobStorageService = blobStorageService;
    }

    @Transactional // Garante a atomicidade da operação
    public void createPost(CreatePostRequest request, MultipartFile foto, String userEmail) {
        try {
            User usuario = userRepository.findByEmail(userEmail)
                    .orElseThrow(() -> new UsernameNotFoundException("Usuário não encontrado: " + userEmail));

            String urlDaFoto = null;
            if (blobStorageService.isPresent() && foto != null && !foto.isEmpty()) {
                urlDaFoto = blobStorageService.get().uploadFile(foto);
            }

            Post novoPost = new Post();
            novoPost.setDescricao(request.getDescricao());
            novoPost.setUsuario(usuario);
            novoPost.setCaminhoFoto(urlDaFoto);
            novoPost.setPublico(request.isPublico());

            if (request.getBeachId() != null) {
                Beach beach = beachRepository.findById(request.getBeachId())
                        .orElseThrow(() -> new ResourceNotFoundException("Praia não encontrada", null)); // Use exceções específicas
                novoPost.setBeach(beach);
            }

            postRepository.save(novoPost);
        } catch (IOException e) {
            throw new RuntimeException("Falha ao fazer upload do arquivo.", e);
        }
    }

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
        if (post.isPublico() || (requesterEmail != null && post.getUsuario().getEmail().equals(requesterEmail))) {
            return Optional.of(convertToDto(post));
        }

        // se o nao for o dono do post e o post for privado retorna null
        return Optional.empty();
    }

    public void updatePost(Long id, String descricao, String userEmail) {
        Post post = postRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Post não encontrado"));

        // Verifica se o usuário é o dono do post
        if (!post.getUsuario().getEmail().equals(userEmail)) {
            throw new SecurityException("Usuário não tem permissão para editar este post");
        }

        // Atualiza apenas a descrição do post
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

        // Converter usuário
        UserDTO userDTO = new UserDTO();
        userDTO.setId(post.getUsuario().getId());
        userDTO.setUsername(post.getUsuario().getUsername());
        userDTO.setEmail(post.getUsuario().getEmail());
        postDTO.setUsuario(userDTO);

        // Converter praia se existir
        if (post.getBeach() != null) {
            BeachDTO beachDTO = new BeachDTO();
            beachDTO.setId(post.getBeach().getId());
            beachDTO.setNome(post.getBeach().getNome());
            beachDTO.setDescricao(post.getBeach().getDescricao());
            beachDTO.setLocalizacao(post.getBeach().getLocalizacao());
            beachDTO.setCaminhoFoto(post.getBeach().getCaminhoFoto());
            postDTO.setBeach(beachDTO);
        }

        List<CommentDTO> commentDTOs = post.getComments().stream()
                .map(comment -> {
                    CommentDTO dto = new CommentDTO();
                    dto.setId(comment.getId());
                    dto.setTexto(comment.getTexto());
                    dto.setData(comment.getData());

                    UserDTO commentUserDTO = new UserDTO();
                    commentUserDTO.setId(comment.getUsuario().getId());
                    commentUserDTO.setUsername(comment.getUsuario().getUsername());
                    commentUserDTO.setEmail(comment.getUsuario().getEmail());
                    dto.setUsuario(commentUserDTO);

                    return dto;
                })
                .collect(Collectors.toList());
        postDTO.setComments(commentDTOs);

        return postDTO;
    }
}