package com.soulsurf.backend.modules.post.service;

import com.soulsurf.backend.modules.notification.event.NotificationEvent;
import com.soulsurf.backend.modules.post.entity.Like;
import com.soulsurf.backend.modules.post.entity.Post;
import com.soulsurf.backend.modules.post.repository.LikeRepository;
import com.soulsurf.backend.modules.post.repository.PostRepository;
import com.soulsurf.backend.modules.user.entity.User;
import com.soulsurf.backend.modules.user.repository.UserRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
@Slf4j
public class LikeService {

    private final LikeRepository likeRepository;
    private final PostRepository postRepository;
    private final UserRepository userRepository;
    private final SimpMessagingTemplate messagingTemplate;
    private final ApplicationEventPublisher eventPublisher;

    public LikeService(LikeRepository likeRepository,
            PostRepository postRepository,
            UserRepository userRepository,
            SimpMessagingTemplate messagingTemplate,
            ApplicationEventPublisher eventPublisher) {
        this.likeRepository = likeRepository;
        this.postRepository = postRepository;
        this.userRepository = userRepository;
        this.messagingTemplate = messagingTemplate;
        this.eventPublisher = eventPublisher;
    }

    @CacheEvict(value = { "postById", "publicFeed", "followingPosts", "userPosts", "beachPosts" }, allEntries = true)
    @Transactional
    public boolean toggleLike(Long postId, String userEmail) {
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new RuntimeException("Post nao encontrado"));

        User usuario = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new UsernameNotFoundException("Usuario nao encontrado"));

        Optional<Like> existingLike = likeRepository.findByPostAndUsuario(post, usuario);

        boolean isLiked;
        if (existingLike.isPresent()) {
            likeRepository.delete(existingLike.get());
            isLiked = false;
        } else {
            Like like = new Like();
            like.setPost(post);
            like.setUsuario(usuario);
            likeRepository.save(like);
            isLiked = true;

            if (!usuario.getId().equals(post.getUsuario().getId())) {
                log.info(
                        "Like notification event published: postId={}, sender={}, recipient={}",
                        postId,
                        usuario.getUsername(),
                        post.getUsuario().getUsername());
                eventPublisher.publishEvent(NotificationEvent.like(usuario.getEmail(), postId));
            } else {
                log.debug("Skipping like notification for own post: postId={}, user={}", postId, usuario.getUsername());
            }
        }

        long likesCount = likeRepository.countByPost(post);

        LikeEvent event = new LikeEvent(postId, likesCount, usuario.getUsername(), isLiked);
        messagingTemplate.convertAndSend("/topic/posts/" + postId + "/likes", event);

        return isLiked;
    }

    public long countLikes(Long postId) {
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new RuntimeException("Post nao encontrado"));
        return likeRepository.countByPost(post);
    }

    public boolean hasUserLiked(Long postId, String userEmail) {
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new RuntimeException("Post nao encontrado"));

        User usuario = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new UsernameNotFoundException("Usuario nao encontrado"));

        return likeRepository.existsByPostAndUsuario(post, usuario);
    }

    public static class LikeEvent {
        private Long postId;
        private long likesCount;
        private String username;
        private boolean liked;

        public LikeEvent(Long postId, long likesCount, String username, boolean liked) {
            this.postId = postId;
            this.likesCount = likesCount;
            this.username = username;
            this.liked = liked;
        }

        public Long getPostId() {
            return postId;
        }

        public long getLikesCount() {
            return likesCount;
        }

        public String getUsername() {
            return username;
        }

        public boolean isLiked() {
            return liked;
        }
    }
}
