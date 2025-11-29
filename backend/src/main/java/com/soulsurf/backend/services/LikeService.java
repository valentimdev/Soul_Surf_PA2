package com.soulsurf.backend.services;

import com.soulsurf.backend.entities.Like;
import com.soulsurf.backend.entities.Post;
import com.soulsurf.backend.entities.User;
import com.soulsurf.backend.repository.LikeRepository;
import com.soulsurf.backend.repository.PostRepository;
import com.soulsurf.backend.repository.UserRepository;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
public class LikeService {

    private final LikeRepository likeRepository;
    private final PostRepository postRepository;
    private final UserRepository userRepository;
    private final SimpMessagingTemplate messagingTemplate; // ★ NOVO

    public LikeService(LikeRepository likeRepository,
                       PostRepository postRepository,
                       UserRepository userRepository,
                       SimpMessagingTemplate messagingTemplate) { // ★ NOVO
        this.likeRepository = likeRepository;
        this.postRepository = postRepository;
        this.userRepository = userRepository;
        this.messagingTemplate = messagingTemplate; // ★ NOVO
    }

    @CacheEvict(value = {"postById", "publicFeed", "followingPosts", "userPosts"}, allEntries = true)
    @Transactional
    public boolean toggleLike(Long postId, String userEmail) {
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new RuntimeException("Post não encontrado"));

        User usuario = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new UsernameNotFoundException("Usuário não encontrado"));

        Optional<Like> existingLike = likeRepository.findByPostAndUsuario(post, usuario);

        boolean isLiked;
        if (existingLike.isPresent()) {
            // Remove o like
            likeRepository.delete(existingLike.get());
            isLiked = false; // Não está mais curtido
        } else {
            // Adiciona o like
            Like like = new Like();
            like.setPost(post);
            like.setUsuario(usuario);
            likeRepository.save(like);
            isLiked = true; // Está curtido
        }

        long likesCount = likeRepository.countByPost(post);

        // ★★★ Envia atualização em tempo real para todos que estão vendo o post ★★★
        LikeEvent event = new LikeEvent(postId, likesCount, usuario.getUsername(), isLiked);
        messagingTemplate.convertAndSend("/topic/posts/" + postId + "/likes", event);

        return isLiked;
    }

    public long countLikes(Long postId) {
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new RuntimeException("Post não encontrado"));
        return likeRepository.countByPost(post);
    }

    public boolean hasUserLiked(Long postId, String userEmail) {
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new RuntimeException("Post não encontrado"));

        User usuario = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new UsernameNotFoundException("Usuário não encontrado"));

        return likeRepository.existsByPostAndUsuario(post, usuario);
    }

    // ★ DTO simples para mandar pelo WebSocket
    public static class LikeEvent {
        private Long postId;
        private long likesCount;
        private String username; // quem curtiu/descurtiu
        private boolean liked;   // true = curtiu, false = retirou

        public LikeEvent(Long postId, long likesCount, String username, boolean liked) {
            this.postId = postId;
            this.likesCount = likesCount;
            this.username = username;
            this.liked = liked;
        }

        public Long getPostId() { return postId; }
        public long getLikesCount() { return likesCount; }
        public String getUsername() { return username; }
        public boolean isLiked() { return liked; }
    }
}
