package com.soulsurf.backend.services;

import com.soulsurf.backend.dto.CommentDTO;
import com.soulsurf.backend.dto.UserDTO;
import com.soulsurf.backend.entities.Comment;
import com.soulsurf.backend.entities.Post;
import com.soulsurf.backend.entities.User;
import com.soulsurf.backend.repository.CommentRepository;
import com.soulsurf.backend.repository.PostRepository;
import com.soulsurf.backend.repository.UserRepository;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Service
public class CommentService {

    private final CommentRepository commentRepository;
    private final PostRepository postRepository;
    private final UserRepository userRepository;
    private final NotificationService notificationService;
    private final SimpMessagingTemplate messagingTemplate; // ★ NOVO

    public CommentService(CommentRepository commentRepository,
                          PostRepository postRepository,
                          UserRepository userRepository,
                          NotificationService notificationService,
                          SimpMessagingTemplate messagingTemplate) { // ★ NOVO
        this.commentRepository = commentRepository;
        this.postRepository = postRepository;
        this.userRepository = userRepository;
        this.notificationService = notificationService;
        this.messagingTemplate = messagingTemplate; // ★ NOVO
    }

    @CacheEvict(value = {"postById"}, allEntries = true)
    public CommentDTO createComment(Long postId, Long parentId, String texto, String userEmail) {
        User usuario = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new UsernameNotFoundException("Usuário não encontrado"));

        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new RuntimeException("Post não encontrado"));

        Comment comment = new Comment();
        comment.setTexto(texto);
        comment.setUsuario(usuario);
        comment.setPost(post);

        if (parentId != null) {
            Comment parentComment = commentRepository.findById(parentId)
                    .orElseThrow(() -> new RuntimeException("Comentário pai não encontrado"));
            comment.setParentComment(parentComment);
        }

        comment = commentRepository.save(comment);

        processarMencoes(comment, usuario.getUsername());

        if (parentId == null) {
            notificationService.createCommentNotification(
                    usuario.getUsername(),
                    postId,
                    comment.getId()
            );
        } else {
            notificationService.createReplyNotification(
                    usuario.getUsername(),
                    postId,
                    comment.getId(),
                    parentId
            );
        }

        CommentDTO dto = convertToDto(comment);

        // ★★★ Envia novo comentário em tempo real ★★★
        CommentEvent event = new CommentEvent("CREATED", postId, dto);
        messagingTemplate.convertAndSend("/topic/posts/" + postId + "/comments", event);

        return dto;
    }

    public void processarMencoes(Comment comment, String senderUsername) {
        Pattern pattern = Pattern.compile("@(\\w+)");
        Matcher matcher = pattern.matcher(comment.getTexto());

        while (matcher.find()) {
            String mentionedUsername = matcher.group(1);

            userRepository.findByUsername(mentionedUsername).ifPresent(mentionedUser -> {
                notificationService.createMentionNotification(
                        senderUsername,
                        mentionedUsername,
                        comment.getPost().getId(),
                        comment.getId()
                );
            });
        }
    }

    public List<CommentDTO> getPostComments(Long postId) {
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new RuntimeException("Post não encontrado"));

        return commentRepository.findByPostAndParentCommentIsNullOrderByDataDesc(post).stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    @CacheEvict(value = {"postById"}, allEntries = true)
    public CommentDTO updateComment(Long postId, Long commentId, String texto, String userEmail) {
        Comment comment = validateAndGetComment(postId, commentId, userEmail);

        comment.setTexto(texto);
        comment = commentRepository.save(comment);

        User usuario = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new UsernameNotFoundException("Usuário não encontrado"));
        processarMencoes(comment, usuario.getUsername());

        CommentDTO dto = convertToDto(comment);

        // ★ Enviar evento de atualização
        CommentEvent event = new CommentEvent("UPDATED", postId, dto);
        messagingTemplate.convertAndSend("/topic/posts/" + postId + "/comments", event);

        return dto;
    }

    @CacheEvict(value = {"postById"}, allEntries = true)
    public void deleteComment(Long postId, Long commentId, String userEmail) {
        Comment comment = validateAndGetComment(postId, commentId, userEmail);

        commentRepository.delete(comment);

        // ★ Enviar evento de remoção
        CommentDTO dto = new CommentDTO();
        dto.setId(commentId);
        // front pode usar só o id para remover da lista

        CommentEvent event = new CommentEvent("DELETED", postId, dto);
        messagingTemplate.convertAndSend("/topic/posts/" + postId + "/comments", event);
    }

    private Comment validateAndGetComment(Long postId, Long commentId, String userEmail) {
        postRepository.findById(postId)
                .orElseThrow(() -> new RuntimeException("Post não encontrado"));

        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new RuntimeException("Comentário não encontrado"));

        User usuario = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new UsernameNotFoundException("Usuário não encontrado"));

        if (!comment.getPost().getId().equals(postId)) {
            throw new RuntimeException("Comentário não pertence a este post");
        }

        if (!comment.getUsuario().equals(usuario)) {
            throw new RuntimeException("Usuário não autorizado para realizar esta ação");
        }

        return comment;
    }

    private CommentDTO convertToDto(Comment comment) {
        CommentDTO dto = new CommentDTO();
        dto.setId(comment.getId());
        dto.setTexto(comment.getTexto());
        dto.setData(comment.getData());

        if (comment.getParentComment() != null) {
            dto.setParentId(comment.getParentComment().getId());
        }

        var userDTO = new UserDTO();
        userDTO.setId(comment.getUsuario().getId());
        userDTO.setUsername(comment.getUsuario().getUsername());
        userDTO.setEmail(comment.getUsuario().getEmail());
        dto.setUsuario(userDTO);

        dto.setReplies(comment.getReplies().stream()
                .map(this::convertToDto)
                .collect(Collectors.toList()));

        return dto;
    }

    // ★ DTO de evento para o WebSocket
    public static class CommentEvent {
        private String type;      // CREATED, UPDATED, DELETED
        private Long postId;
        private CommentDTO comment;

        public CommentEvent(String type, Long postId, CommentDTO comment) {
            this.type = type;
            this.postId = postId;
            this.comment = comment;
        }

        public String getType() { return type; }
        public Long getPostId() { return postId; }
        public CommentDTO getComment() { return comment; }
    }
}
