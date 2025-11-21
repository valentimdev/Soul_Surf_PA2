package com.soulsurf.backend.services;

import com.azure.core.exception.ResourceNotFoundException;
import com.soulsurf.backend.dto.BeachDTO;
import com.soulsurf.backend.dto.CommentDTO;
import com.soulsurf.backend.dto.CreatePostRequest;
import com.soulsurf.backend.dto.PostDTO;
import com.soulsurf.backend.dto.UserDTO;
import com.soulsurf.backend.entities.*;
import com.soulsurf.backend.repository.*;

import jakarta.transaction.Transactional;

import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.context.annotation.Lazy;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class PostService {

    private final PostRepository postRepository;
    private final UserRepository userRepository;
    private final BeachRepository beachRepository;
    private final Optional<BlobStorageService> blobStorageService;
    private final LikeService likeService;
    private final NotificationRepository notificationRepository;
    private final CommentRepository commentRepository;
    private final LikeRepository likeRepository;

    public PostService(PostRepository postRepository,
                       UserRepository userRepository,
                       BeachRepository beachRepository,
                       Optional<BlobStorageService> blobStorageService,
                       @Lazy LikeService likeService,
                       NotificationRepository notificationRepository,
                       CommentRepository commentRepository,
                       LikeRepository likeRepository) {
        this.postRepository = postRepository;
        this.userRepository = userRepository;
        this.beachRepository = beachRepository;
        this.blobStorageService = blobStorageService;
        this.likeService = likeService;
        this.notificationRepository = notificationRepository;
        this.commentRepository = commentRepository;
        this.likeRepository = likeRepository;
    }

    @Transactional
    @CacheEvict(value = {"publicFeed", "userPosts", "followingPosts"}, allEntries = true)
    public PostDTO createPost(CreatePostRequest request, MultipartFile foto, String userEmail) {
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
                        .orElseThrow(() -> new ResourceNotFoundException("Praia não encontrada", null));
                novoPost.setBeach(beach);
            }

            postRepository.save(novoPost);

            return convertToDto(novoPost, userEmail); // <-- retorna o PostDTO
        } catch (IOException e) {
            throw new RuntimeException("Falha ao fazer upload do arquivo.", e);
        }
    }

    @Cacheable(value = "publicFeed", key = "#pageable.pageNumber + '_' + #pageable.pageSize", unless = "#result.content.isEmpty()")
    public Page<PostDTO> getPublicFeed(Pageable pageable) {
        Page<Post> posts = postRepository.findByPublicoIsTrue(pageable);
        return posts.map(post -> convertToDto(post, null));
    }

    @Cacheable(value = "followingPosts", key = "#userEmail + '_' + #pageable.pageNumber + '_' + #pageable.pageSize", unless = "#result.content.isEmpty()")
    public Page<PostDTO> getFollowingPosts(String userEmail, Pageable pageable) {
        User currentUser = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new UsernameNotFoundException("Usuário não encontrado: " + userEmail));

        List<User> followingUsers = currentUser.getSeguindo();

        if (followingUsers.isEmpty()) {
            return Page.empty(pageable);
        }

        Page<Post> posts = postRepository.findByUsuarioIn(followingUsers, pageable);

        return posts.map(post -> convertToDto(post, userEmail));
    }

    @Cacheable(value = "userPosts", key = "#userEmail + '_' + #pageable.pageNumber + '_' + #pageable.pageSize", unless = "#result.content.isEmpty()")
    public Page<PostDTO> getPostsByUserEmail(String userEmail, Pageable pageable) {
        User usuario = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new UsernameNotFoundException("Usuário não encontrado com o e-mail: " + userEmail));
    
        Page<Post> posts = postRepository.findByUsuario(usuario, pageable);
    
        return posts.map(post -> convertToDto(post, userEmail));
    }

    @Cacheable(value = "postById", key = "#id + '_' + (#requesterEmail ?: 'anonymous')")
    public Optional<PostDTO> getPostById(Long id, String requesterEmail) {
        Optional<Post> postOptional = postRepository.findById(id);

        if (postOptional.isEmpty()) {
            return Optional.empty();
        }

        Post post = postOptional.get();
        if (post.isPublico() || (requesterEmail != null && post.getUsuario().getEmail().equals(requesterEmail))) {
            return Optional.of(convertToDto(post, requesterEmail));
        }

        return Optional.empty();
    }

    @CacheEvict(value = {"publicFeed", "userPosts", "followingPosts", "postById"}, allEntries = true)
    public void updatePost(Long id, String descricao, String userEmail) {
        Post post = postRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Post não encontrado"));

        if (!post.getUsuario().getEmail().equals(userEmail)) {
            throw new SecurityException("Usuário não tem permissão para editar este post");
        }

        post.setDescricao(descricao);
        postRepository.save(post);
    }

    private CommentDTO convertCommentToDto(Comment comment) {
        CommentDTO dto = new CommentDTO();
        dto.setId(comment.getId());
        dto.setTexto(comment.getTexto());
        dto.setData(comment.getData());

        if (comment.getParentComment() != null) {
            dto.setParentId(comment.getParentComment().getId());
        }

        UserDTO userDTO = new UserDTO();
        userDTO.setId(comment.getUsuario().getId());
        userDTO.setUsername(comment.getUsuario().getUsername());
        userDTO.setEmail(comment.getUsuario().getEmail());
        dto.setUsuario(userDTO);

        dto.setReplies(comment.getReplies().stream()
                .map(this::convertCommentToDto)
                .collect(Collectors.toList()));

        return dto;
    }

    private PostDTO convertToDto(Post post) {
        return convertToDto(post, null);
    }

    private PostDTO convertToDto(Post post, String currentUserEmail) {
        PostDTO postDTO = new PostDTO();
        postDTO.setId(post.getId());
        postDTO.setPublico(post.isPublico());
        postDTO.setDescricao(post.getDescricao());
        postDTO.setCaminhoFoto(post.getCaminhoFoto());
        postDTO.setData(post.getData());

        User usuario = post.getUsuario();
        UserDTO userDTO = new UserDTO();
        userDTO.setId(usuario.getId());
        userDTO.setUsername(usuario.getUsername());
        userDTO.setEmail(usuario.getEmail());
        userDTO.setFotoPerfil(usuario.getFotoPerfil());
        userDTO.setFotoCapa(usuario.getFotoCapa());
        userDTO.setBio(usuario.getBio());
        userDTO.setSeguidoresCount(usuario.getSeguidores().size());
        userDTO.setSeguindoCount(usuario.getSeguindo().size());
        postDTO.setUsuario(userDTO);

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
                .filter(comment -> comment.getParentComment() == null)
                .map(this::convertCommentToDto)
                .collect(Collectors.toList());
        postDTO.setComments(commentDTOs);

        // Adicionar contagem de likes e status do like do usuário atual
        postDTO.setLikesCount(likeService.countLikes(post.getId()));
        if (currentUserEmail != null) {
            postDTO.setLikedByCurrentUser(likeService.hasUserLiked(post.getId(), currentUserEmail));
        } else {
            postDTO.setLikedByCurrentUser(false);
        }
        postDTO.setCommentsCount(post.getComments().size());

        return postDTO;
    }

    @CacheEvict(value = {"publicFeed", "userPosts", "followingPosts", "postById"}, allEntries = true)
    @Transactional
    public void deletePost(Long postId, User requester) {

        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new IllegalArgumentException("Post não encontrado"));

        if (!post.getUsuario().getId().equals(requester.getId())) {
            throw new SecurityException("Não autorizado");
        }

        notificationRepository.deleteByPost(post);
        List<Comment> comments = commentRepository.findByPostOrderByDataDesc(post);
        for (Comment c : comments) {
            notificationRepository.deleteByComment(c);
        }
        likeRepository.deleteAllByPost(post);
        commentRepository.deleteAll(comments);
        postRepository.delete(post);
    }
}