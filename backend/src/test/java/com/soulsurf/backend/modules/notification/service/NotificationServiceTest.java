package com.soulsurf.backend.modules.notification.service;

import com.soulsurf.backend.modules.notification.dto.NotificationDTO;
import com.soulsurf.backend.modules.notification.entity.Notification;
import com.soulsurf.backend.modules.notification.mapper.NotificationMapper;
import com.soulsurf.backend.modules.notification.repository.NotificationRepository;
import com.soulsurf.backend.modules.comment.entity.Comment;
import com.soulsurf.backend.modules.post.entity.Post;
import com.soulsurf.backend.modules.user.entity.User;
import com.soulsurf.backend.modules.user.repository.UserRepository;
import com.soulsurf.backend.modules.post.repository.PostRepository;
import com.soulsurf.backend.modules.comment.repository.CommentRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.messaging.simp.SimpMessagingTemplate;

import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class NotificationServiceTest {

    @Mock
    private NotificationRepository notificationRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private PostRepository postRepository;

    @Mock
    private CommentRepository commentRepository;

    @Mock
    private SimpMessagingTemplate messagingTemplate;

    @Mock
    private NotificationMapper notificationMapper;

    @InjectMocks
    private NotificationService notificationService;

    private User targetUser;
    private User actorUser;

    @BeforeEach
    void setUp() {
        targetUser = new User();
        targetUser.setId(1L);
        targetUser.setUsername("target");

        actorUser = new User();
        actorUser.setId(2L);
        actorUser.setUsername("actor");
        actorUser.setEmail("actor@example.com");
    }

    @Test
    void testCreateLikeNotification() {
        Post post = new Post();
        post.setId(10L);
        post.setUsuario(targetUser);

        when(userRepository.findByEmail("actor@example.com")).thenReturn(Optional.of(actorUser));
        when(postRepository.findById(10L)).thenReturn(Optional.of(post));

        Notification mockSaved = new Notification();
        mockSaved.setRecipient(targetUser);
        when(notificationRepository.save(any(Notification.class))).thenReturn(mockSaved);
        when(notificationMapper.toDto(any(Notification.class))).thenReturn(new NotificationDTO());

        notificationService.createLikeNotification("actor@example.com", 10L);

        verify(notificationRepository, times(1)).save(any(Notification.class));
        verify(messagingTemplate, times(1)).convertAndSend(eq("/topic/notifications/target"), any(Object.class));
    }

    @Test
    void testCreateCommentNotification() {
        Post post = new Post();
        post.setId(20L);
        post.setUsuario(targetUser);

        Comment comment = new Comment();
        comment.setId(30L);
        comment.setPost(post);

        when(postRepository.findById(20L)).thenReturn(Optional.of(post));
        when(userRepository.findByEmail("actor@example.com")).thenReturn(Optional.of(actorUser));
        when(commentRepository.findById(30L)).thenReturn(Optional.of(comment));

        Notification mockSaved = new Notification();
        mockSaved.setRecipient(targetUser);
        when(notificationRepository.save(any(Notification.class))).thenReturn(mockSaved);
        when(notificationMapper.toDto(any(Notification.class))).thenReturn(new NotificationDTO());

        notificationService.createCommentNotification("actor@example.com", 20L, 30L);

        verify(notificationRepository, times(1)).save(any(Notification.class));
        verify(messagingTemplate, times(1)).convertAndSend(eq("/topic/notifications/target"), any(Object.class));
    }
}
