package com.soulsurf.backend.services;

import com.soulsurf.backend.entities.Comment;
import com.soulsurf.backend.entities.Post;
import com.soulsurf.backend.entities.User;
import com.soulsurf.backend.repository.CommentRepository;
import com.soulsurf.backend.repository.PostRepository;
import com.soulsurf.backend.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AdminService {

    private final UserRepository userRepository;
    private final PostRepository postRepository;
    private final CommentRepository commentRepository;

    private final AdminAuditService adminAuditService;

    public AdminService(UserRepository userRepository,
                        PostRepository postRepository,
                        CommentRepository commentRepository,
                        AdminAuditService adminAuditService) {
        this.userRepository = userRepository;
        this.postRepository = postRepository;
        this.commentRepository = commentRepository;
        this.adminAuditService = adminAuditService;
    }

    @Transactional
    public void deleteUser(Long userId, String actorEmail) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("Usuário não encontrado"));
        userRepository.delete(user);
        adminAuditService.log(actorEmail, "DELETE_USER", "USER", userId, null);
    }

    @Transactional
    public void deletePost(Long postId, String actorEmail) {
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new IllegalArgumentException("Post não encontrado"));
        postRepository.delete(post);
        adminAuditService.log(actorEmail, "DELETE_POST", "POST", postId, null);
    }

    @Transactional
    public void deleteComment(Long commentId, String actorEmail) {
        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new IllegalArgumentException("Comentário não encontrado"));
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
}


