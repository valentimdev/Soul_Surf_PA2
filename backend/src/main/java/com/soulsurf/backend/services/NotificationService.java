package com.soulsurf.backend.services;

import com.soulsurf.backend.dto.NotificationDTO;
import com.soulsurf.backend.dto.UserDTO;
import com.soulsurf.backend.entities.*;
import com.soulsurf.backend.repository.CommentRepository;
import com.soulsurf.backend.repository.NotificationRepository;
import com.soulsurf.backend.repository.PostRepository;
import com.soulsurf.backend.repository.UserRepository;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class NotificationService {

    private final NotificationRepository notificationRepository;
    private final UserRepository userRepository;
    private final PostRepository postRepository;
    private final CommentRepository commentRepository;
    private final SimpMessagingTemplate messagingTemplate; // ★ WebSocket para tempo real

    public NotificationService(NotificationRepository notificationRepository, UserRepository userRepository,
                               PostRepository postRepository, CommentRepository commentRepository,
                               SimpMessagingTemplate messagingTemplate) {
        this.notificationRepository = notificationRepository;
        this.userRepository = userRepository;
        this.postRepository = postRepository;
        this.commentRepository = commentRepository;
        this.messagingTemplate = messagingTemplate;
    }

    // ★ Método para enviar notificação em tempo real via WebSocket
    private void sendRealTimeNotification(Notification notification) {
        try {
            NotificationDTO dto = convertToDTO(notification);
            String destination = "/topic/notifications/" + notification.getRecipient().getUsername();
            messagingTemplate.convertAndSend(destination, dto);
        } catch (Exception e) {
            // Log do erro mas não impede a criação da notificação
            System.err.println("Erro ao enviar notificação em tempo real: " + e.getMessage());
        }
    }

    @Transactional
    public void createMentionNotification(String senderUsername, String recipientUsername, Long postId, Long commentId) {
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
        notification.setType(NotificationType.MENTION); // ✅ Enum
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
        sendRealTimeNotification(saved); // ★ Enviar em tempo real
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
        sendRealTimeNotification(saved); // ★ Enviar em tempo real
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
        sendRealTimeNotification(saved); // ★ Enviar em tempo real
    }

    @Transactional
    public void createLikeNotification(String senderUsername, Long postId) {
        User sender = userRepository.findByUsername(senderUsername)
                .orElseThrow(() -> new RuntimeException("Usuário remetente não encontrado"));

        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new RuntimeException("Post não encontrado"));

        // Não notificar se for o próprio usuário curtindo seu post
        if (sender.getId().equals(post.getUsuario().getId())) {
            return;
        }

        Notification notification = new Notification();
        notification.setSender(sender);
        notification.setRecipient(post.getUsuario());
        notification.setType(NotificationType.LIKE);
        notification.setPost(post);

        Notification saved = notificationRepository.save(notification);
        sendRealTimeNotification(saved); // ★ Enviar em tempo real
    }

    @Transactional
    public List<NotificationDTO> getUserNotifications(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Usuário não encontrado"));

        return notificationRepository.findByRecipientOrderByCreatedAtDesc(user).stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    @Transactional
    public int getUnreadCount(String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("Usuário não encontrado"));

        return notificationRepository.countByRecipientAndReadFalse(user);
    }

    @Transactional
    public void markAsRead(Long notificationId, String usernameOrEmail) {
        // userDetails.getUsername() retorna email, então busca por email
        User user = userRepository.findByEmail(usernameOrEmail)
                .orElseThrow(() -> new RuntimeException("Usuário não encontrado"));

        Notification notification = notificationRepository.findById(notificationId)
                .orElseThrow(() -> new RuntimeException("Notificação não encontrada"));

        if (!notification.getRecipient().getId().equals(user.getId())) {
            throw new RuntimeException("Esta notificação não pertence ao usuário atual");
        }

        notification.setRead(true);
        notificationRepository.save(notification);
    }

    private NotificationDTO convertToDTO(Notification notification) {
        NotificationDTO dto = new NotificationDTO();
        dto.setId(notification.getId());

        UserDTO senderDTO = new UserDTO();
        senderDTO.setId(notification.getSender().getId());
        senderDTO.setUsername(notification.getSender().getUsername());
        senderDTO.setEmail(notification.getSender().getEmail());
        senderDTO.setFotoPerfil(notification.getSender().getFotoPerfil());
        dto.setSender(senderDTO);

        dto.setType(notification.getType().name());

        if (notification.getPost() != null) {
            dto.setPostId(notification.getPost().getId());
        }

        if (notification.getComment() != null) {
            dto.setCommentId(notification.getComment().getId());
        }

        dto.setRead(notification.isRead());
        dto.setCreatedAt(notification.getCreatedAt());

        String message = switch (notification.getType()) {
            case MENTION -> notification.getSender().getUsername() + " mencionou você em um comentário";
            case COMMENT -> notification.getSender().getUsername() + " comentou em seu post";
            case REPLY -> notification.getSender().getUsername() + " respondeu ao seu comentário";
            case LIKE -> notification.getSender().getUsername() + " curtiu seu post";
        };

        dto.setMessage(message);
        return dto;
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
