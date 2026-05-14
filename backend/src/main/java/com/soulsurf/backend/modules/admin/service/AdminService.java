package com.soulsurf.backend.modules.admin.service;

import com.soulsurf.backend.modules.comment.entity.Comment;
import com.soulsurf.backend.modules.comment.dto.CommentDTO;
import com.soulsurf.backend.modules.comment.mapper.CommentMapper;
import com.soulsurf.backend.modules.post.entity.Post;
import com.soulsurf.backend.modules.post.dto.PostDTO;
import com.soulsurf.backend.modules.post.mapper.PostMapper;
import com.soulsurf.backend.modules.user.entity.User;
import com.soulsurf.backend.modules.comment.repository.CommentRepository;
import com.soulsurf.backend.modules.post.repository.PostRepository;
import com.soulsurf.backend.modules.user.repository.UserRepository;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.soulsurf.backend.modules.notification.service.NotificationService;

import java.util.ArrayList;
import java.util.List;

@Service
public class AdminService {

    private final UserRepository userRepository;
    private final PostRepository postRepository;
    private final CommentRepository commentRepository;
    private final AdminAuditService adminAuditService;
    private final NotificationService notificationService;
    private final PostMapper postMapper;
    private final CommentMapper commentMapper;

    public AdminService(UserRepository userRepository,
            PostRepository postRepository,
            CommentRepository commentRepository,
            AdminAuditService adminAuditService,
            NotificationService notificationService,
            PostMapper postMapper,
            CommentMapper commentMapper) {
        this.userRepository = userRepository;
        this.postRepository = postRepository;
        this.commentRepository = commentRepository;
        this.adminAuditService = adminAuditService;
        this.notificationService = notificationService;
        this.postMapper = postMapper;
        this.commentMapper = commentMapper;
    }

    @Transactional(readOnly = true)
    public PostDTO getPost(Long postId, String actorEmail) {
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new IllegalArgumentException("Post nao encontrado"));
        return postMapper.toDto(post, actorEmail);
    }

    @Transactional(readOnly = true)
    public List<CommentDTO> getPostComments(Long postId) {
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new IllegalArgumentException("Post nao encontrado"));
        return commentRepository.findByPostAndParentCommentIsNullOrderByDataDesc(post).stream()
                .map(commentMapper::toDto)
                .toList();
    }

    @Transactional
    public void deleteUser(Long userId, String actorEmail) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("Usuário não encontrado"));
        userRepository.delete(user);
        adminAuditService.log(actorEmail, "DELETE_USER", "USER", userId, null);
    }

    @Transactional
    @CacheEvict(value = { "publicFeed", "userPosts", "followingPosts", "postById" }, allEntries = true)
    public void deletePost(Long postId, String actorEmail) {
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new IllegalArgumentException("Post não encontrado"));

        notificationService.deleteNotificationsByPost(post);

        postRepository.delete(post);
        adminAuditService.log(actorEmail, "DELETE_POST", "POST", postId, null);
    }

    @Transactional
    @CacheEvict(value = { "postById" }, allEntries = true)
    public void deleteComment(Long commentId, String actorEmail) {
        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new IllegalArgumentException("Comentário não encontrado"));

        notificationService.deleteNotificationsByComments(collectCommentTree(comment));

        commentRepository.delete(comment);
        adminAuditService.log(actorEmail, "DELETE_COMMENT", "COMMENT", commentId, null);
    }

    @Transactional
    public void promoteToAdmin(Long userId, String actorEmail) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("Usuário não encontrado"));
        user.setAdmin(true);
        userRepository.save(user);
        adminAuditService.log(actorEmail, "PROMOTE_TO_ADMIN", "USER", userId, null);
    }

    @Transactional
    public void demoteFromAdmin(Long userId, String actorEmail) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("Usuário não encontrado"));
        user.setAdmin(false);
        userRepository.save(user);
        adminAuditService.log(actorEmail, "DEMOTE_FROM_ADMIN", "USER", userId, null);
    }

    @Transactional
    public void banUser(Long userId, String actorEmail) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("Usuário não encontrado"));
        user.setBanned(true);
        userRepository.save(user);
        adminAuditService.log(actorEmail, "BAN_USER", "USER", userId, null);
    }

    @Transactional
    public void unbanUser(Long userId, String actorEmail) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("Usuário não encontrado"));
        user.setBanned(false);
        userRepository.save(user);
        adminAuditService.log(actorEmail, "UNBAN_USER", "USER", userId, null);
    }

    private List<Comment> collectCommentTree(Comment comment) {
        List<Comment> comments = new ArrayList<>();
        comments.add(comment);
        for (Comment reply : comment.getReplies()) {
            comments.addAll(collectCommentTree(reply));
        }
        return comments;
    }
}

