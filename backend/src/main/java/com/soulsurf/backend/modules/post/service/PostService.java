package com.soulsurf.backend.modules.post.service;

import jakarta.persistence.EntityNotFoundException;
import com.soulsurf.backend.modules.post.controller.CreatePostRequest;
import com.soulsurf.backend.modules.post.dto.PostDTO;
import com.soulsurf.backend.modules.post.entity.Post;
import com.soulsurf.backend.modules.post.repository.PostRepository;
import com.soulsurf.backend.modules.post.mapper.PostMapper;
import com.soulsurf.backend.modules.post.repository.LikeRepository;
import com.soulsurf.backend.modules.user.entity.User;
import com.soulsurf.backend.modules.user.repository.UserRepository;
import com.soulsurf.backend.modules.beach.entity.Beach;
import com.soulsurf.backend.modules.beach.repository.BeachRepository;
import com.soulsurf.backend.modules.comment.entity.Comment;
import com.soulsurf.backend.modules.comment.repository.CommentRepository;
import com.soulsurf.backend.modules.notification.repository.NotificationRepository;
import com.soulsurf.backend.core.storage.OracleStorageService;

import jakarta.transaction.Transactional;

import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.io.IOException;
import java.util.List;
import java.util.Optional;

@Service
public class PostService {

    private final PostRepository postRepository;
    private final UserRepository userRepository;
    private final BeachRepository beachRepository;
    private final Optional<OracleStorageService> blobStorageService;
    private final NotificationRepository notificationRepository;
    private final CommentRepository commentRepository;
    private final LikeRepository likeRepository;
    private final PostMapper postMapper;

    public PostService(PostRepository postRepository,
            UserRepository userRepository,
            BeachRepository beachRepository,
            Optional<OracleStorageService> blobStorageService,
            NotificationRepository notificationRepository,
            CommentRepository commentRepository,
            LikeRepository likeRepository,
            PostMapper postMapper) {
        this.postRepository = postRepository;
        this.userRepository = userRepository;
        this.beachRepository = beachRepository;
        this.blobStorageService = blobStorageService;
        this.notificationRepository = notificationRepository;
        this.commentRepository = commentRepository;
        this.likeRepository = likeRepository;
        this.postMapper = postMapper;
    }

    @Transactional
    @CacheEvict(value = { "publicFeed", "userPosts", "followingPosts" }, allEntries = true)
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
                        .orElseThrow(() -> new EntityNotFoundException("Praia não encontrada"));
                novoPost.setBeach(beach);
            }

            postRepository.save(novoPost);

            return postMapper.toDto(novoPost, userEmail);
        } catch (IOException e) {
            throw new RuntimeException("Falha ao fazer upload do arquivo.", e);
        }
    }

    @Cacheable(value = "publicFeed", key = "#pageable.pageNumber + '_' + #pageable.pageSize", unless = "#result.content.isEmpty()")
    public Page<PostDTO> getPublicFeed(Pageable pageable) {
        Page<Post> posts = postRepository.findByPublicoIsTrue(pageable);
        return posts.map(post -> postMapper.toDto(post, null));
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

        return posts.map(post -> postMapper.toDto(post, userEmail));
    }

    @Cacheable(value = "userPosts", key = "#userEmail + '_' + #pageable.pageNumber + '_' + #pageable.pageSize", unless = "#result.content.isEmpty()")
    public Page<PostDTO> getPostsByUserEmail(String userEmail, Pageable pageable) {
        User usuario = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new UsernameNotFoundException("Usuário não encontrado com o e-mail: " + userEmail));

        Page<Post> posts = postRepository.findByUsuario(usuario, pageable);

        return posts.map(post -> postMapper.toDto(post, userEmail));
    }

    @Cacheable(value = "postById", key = "#id + '_' + (#requesterEmail ?: 'anonymous')")
    public Optional<PostDTO> getPostById(Long id, String requesterEmail) {
        Optional<Post> postOptional = postRepository.findById(id);

        if (postOptional.isEmpty()) {
            return Optional.empty();
        }

        Post post = postOptional.get();
        if (post.isPublico() || (requesterEmail != null && post.getUsuario().getEmail().equals(requesterEmail))) {
            return Optional.of(postMapper.toDto(post, requesterEmail));
        }

        return Optional.empty();
    }

    @CacheEvict(value = { "publicFeed", "userPosts", "followingPosts", "postById" }, allEntries = true)
    public void updatePost(Long id, String descricao, String userEmail) {
        Post post = postRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Post não encontrado"));

        if (!post.getUsuario().getEmail().equals(userEmail)) {
            throw new SecurityException("Usuário não tem permissão para editar este post");
        }

        post.setDescricao(descricao);
        postRepository.save(post);
    }

    @CacheEvict(value = { "publicFeed", "userPosts", "followingPosts", "postById" }, allEntries = true)
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
