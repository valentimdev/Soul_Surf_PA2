package com.soulsurf.backend.modules.admin.service;

import com.soulsurf.backend.modules.admin.dto.AdminMetricsDTO;
import com.soulsurf.backend.modules.comment.repository.CommentRepository;
import com.soulsurf.backend.modules.post.repository.LikeRepository;
import com.soulsurf.backend.modules.post.repository.PostRepository;
import com.soulsurf.backend.modules.user.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class AdminMetricsServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PostRepository postRepository;

    @Mock
    private CommentRepository commentRepository;

    @InjectMocks
    private AdminMetricsService adminMetricsService;

    @Test
    void testGetMetrics() {
        when(userRepository.count()).thenReturn(150L);
        when(postRepository.count()).thenReturn(300L);
        when(commentRepository.count()).thenReturn(600L);

        AdminMetricsDTO metrics = adminMetricsService.getMetrics();

        assertNotNull(metrics);
        assertEquals(150L, metrics.getTotalUsers());
        assertEquals(300L, metrics.getTotalPosts());
        assertEquals(600L, metrics.getTotalComments());
    }
}
