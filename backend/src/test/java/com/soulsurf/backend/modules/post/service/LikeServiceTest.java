package com.soulsurf.backend.modules.post.service;

import com.soulsurf.backend.modules.post.entity.Like;
import com.soulsurf.backend.modules.post.entity.Post;
import com.soulsurf.backend.modules.post.repository.LikeRepository;
import com.soulsurf.backend.modules.post.repository.PostRepository;
import com.soulsurf.backend.modules.user.entity.User;
import com.soulsurf.backend.modules.user.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.messaging.simp.SimpMessagingTemplate;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class LikeServiceTest {

    @Mock
    private LikeRepository likeRepository;

    @Mock
    private PostRepository postRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private SimpMessagingTemplate messagingTemplate;

    @InjectMocks
    private LikeService likeService;

    private User testUser;
    private Post testPost;
    private Like testLike;

    @BeforeEach
    void setUp() {
        testUser = new User();
        testUser.setUsername("testuser");
        testUser.setEmail("test@example.com");
        testUser.setId(1L);

        testPost = new Post();
        testPost.setId(10L);
        testPost.setUsuario(testUser);

        testLike = new Like();
        testLike.setId(100L);
        testLike.setPost(testPost);
        testLike.setUsuario(testUser);
    }

    @Test
    void testToggleLike_AddLike() {
        when(postRepository.findById(10L)).thenReturn(Optional.of(testPost));
        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(testUser));
        when(likeRepository.findByPostAndUsuario(testPost, testUser)).thenReturn(Optional.empty());
        when(likeRepository.save(any(Like.class))).thenReturn(testLike);

        boolean isLiked = likeService.toggleLike(10L, "test@example.com");

        assertTrue(isLiked);
        verify(likeRepository, times(1)).save(any(Like.class));
        verify(likeRepository, never()).delete(any(Like.class));
    }

    @Test
    void testToggleLike_RemoveLike() {
        when(postRepository.findById(10L)).thenReturn(Optional.of(testPost));
        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(testUser));
        when(likeRepository.findByPostAndUsuario(testPost, testUser)).thenReturn(Optional.of(testLike));

        boolean isLiked = likeService.toggleLike(10L, "test@example.com");

        assertFalse(isLiked);
        verify(likeRepository, times(1)).delete(testLike);
        verify(likeRepository, never()).save(any(Like.class));
    }
}
