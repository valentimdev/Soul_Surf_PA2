package com.soulsurf.backend.modules.notification.service;

import com.soulsurf.backend.modules.notification.dto.NotificationDTO;
import com.soulsurf.backend.modules.notification.entity.Notification;
import com.soulsurf.backend.modules.notification.mapper.NotificationMapper;
import com.soulsurf.backend.modules.notification.repository.NotificationRepository;
import com.soulsurf.backend.modules.notification.entity.NotificationType;
import com.soulsurf.backend.modules.user.entity.User;
import com.soulsurf.backend.modules.user.repository.UserRepository;
import com.soulsurf.backend.modules.post.entity.Post;
import com.soulsurf.backend.modules.post.repository.PostRepository;
import com.soulsurf.backend.modules.comment.entity.Comment;
import com.soulsurf.backend.modules.comment.repository.CommentRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@Slf4j
public class NotificationService {

    private final NotificationRepository notificationRepository;
    private final UserRepository userRepository;
    private final PostRepository postRepository;
    private final CommentRepository commentRepository;
    private final SimpMessagingTemplate messagingTemplate;
    private final NotificationMapper notificationMapper;

    public NotificationService(NotificationRepository notificationRepository, UserRepository userRepository,
            PostRepository postRepository, CommentRepository commentRepository,
            SimpMessagingTemplate messagingTemplate, NotificationMapper notificationMapper) {
        this.notificationRepository = notificationRepository;
        this.userRepository = userRepository;
        this.postRepository = postRepository;
        this.commentRepository = commentRepository;
        this.messagingTemplate = messagingTemplate;
        this.notificationMapper = notificationMapper;
    }

    private void sendRealTimeNotification(Notification notification) {
        try {
            NotificationDTO dto = notificationMapper.toDto(notification);
            String destination = "/topic/notifications/" + notification.getRecipient().getUsername();
            messagingTemplate.convertAndSend(destination, dto);
        } catch (Exception e) {
            log.error("Erro ao enviar notificação em tempo real: {}", e.getMessage(), e);
        }
    }

    @Transactional
    public void createMentionNotification(String senderUsername, String recipientUsername, Long postId,
            Long commentId) {
        if (senderUsername.equals(recipientUsername)) {
            return;
        }

        User recipient = userRepository.findByUsername(recipientUsername)
                .orElseThrow(() -> new UsernameNotFoundException("Usuário não encontrado: " + recipientUsername));

        User sender = userRepository.findByUsername(senderUsername)
                .orElseThrow(() -> new UsernameNotFoundException("Remetente não encontrado: " + senderUsername));

        Notification notification = new Notification();
        notification.setRecipient(recipient);
        notification.setSender(sender);
        notification.setType(NotificationType.MENTION);
        notification.setRead(false);

        if (postId != null) {
            Post post = postRepository.findById(postId)
                    .orElseThrow(() -> new RuntimeException("Post não encontrado: " + postId));
            notification.setPost(post);
        }

        if (commentId != null) {
            Comment comment = commentRepository.findById(commentId)
                    .orElseThrow(() -> new RuntimeException("Comentário não encontrado: " + commentId));
            notification.setComment(comment);
        }

        Notification saved = notificationRepository.save(notification);
        sendRealTimeNotification(saved);
    }

    @Transactional
    public void createCommentNotification(String senderUsername, Long postId, Long commentId) {
        User sender = userRepository.findByUsername(senderUsername)
                .orElseThrow(() -> new RuntimeException("Usuário remetente não encontrado"));

        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new RuntimeException("Post não encontrado"));

        if (sender.getId().equals(post.getUsuario().getId())) {
            return;
        }

        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new RuntimeException("Comentário não encontrado"));

        Notification notification = new Notification();
        notification.setSender(sender);
        notification.setRecipient(post.getUsuario());
        notification.setType(NotificationType.COMMENT);
        notification.setPost(post);
        notification.setComment(comment);

        Notification saved = notificationRepository.save(notification);
        sendRealTimeNotification(saved);
    }

    @Transactional
    public void createReplyNotification(String senderUsername, Long postId, Long commentId, Long parentCommentId) {
        User sender = userRepository.findByUsername(senderUsername)
                .orElseThrow(() -> new RuntimeException("Usuário remetente não encontrado"));

        Comment parentComment = commentRepository.findById(parentCommentId)
                .orElseThrow(() -> new RuntimeException("Comentário pai não encontrado"));

        if (sender.getId().equals(parentComment.getUsuario().getId())) {
            return;
        }

        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new RuntimeException("Post não encontrado"));

        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new RuntimeException("Comentário não encontrado"));

        Notification notification = new Notification();
        notification.setSender(sender);
        notification.setRecipient(parentComment.getUsuario());
        notification.setType(NotificationType.REPLY);
        notification.setPost(post);
        notification.setComment(comment);

        Notification saved = notificationRepository.save(notification);
        sendRealTimeNotification(saved);
    }

    @Transactional
    public void createLikeNotification(String senderUsername, Long postId) {
        User sender = userRepository.findByUsername(senderUsername)
                .orElseThrow(() -> new RuntimeException("Usuário remetente não encontrado"));

        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new RuntimeException("Post não encontrado"));

        if (sender.getId().equals(post.getUsuario().getId())) {
            return;
        }

        Notification notification = new Notification();
        notification.setSender(sender);
        notification.setRecipient(post.getUsuario());
        notification.setType(NotificationType.LIKE);
        notification.setPost(post);

        Notification saved = notificationRepository.save(notification);
        sendRealTimeNotification(saved);
    }

    @Transactional
    public List<NotificationDTO> getUserNotifications(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Usuário não encontrado"));

        return notificationRepository.findByRecipientOrderByCreatedAtDesc(user).stream()
                .map(notificationMapper::toDto)
                .collect(Collectors.toList());
    }

    @Transactional
    public int getUnreadCount(String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("Usuário não encontrado"));

        return notificationRepository.countByRecipientAndReadFalse(user);
    }

    @Transactional
    public void markAsRead(Long notificationId, String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Usuário não encontrado"));

        Notification notification = notificationRepository.findById(notificationId)
                .orElseThrow(() -> new RuntimeException("Notificação não encontrada"));

        if (!notification.getRecipient().getId().equals(user.getId())) {
            throw new RuntimeException("Esta notificação não pertence ao usuário atual");
        }

        notification.setRead(true);
        notificationRepository.save(notification);
    }

    @Transactional
    public void deleteNotificationsByPost(Post post) {
        notificationRepository.deleteByPost(post);
    }

    @Transactional
    public void deleteNotificationsByComment(Comment comment) {
        notificationRepository.deleteByComment(comment);
    }
}
