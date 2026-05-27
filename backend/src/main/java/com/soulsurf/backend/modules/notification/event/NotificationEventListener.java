package com.soulsurf.backend.modules.notification.event;

import com.soulsurf.backend.modules.notification.entity.NotificationType;
import com.soulsurf.backend.modules.notification.service.NotificationService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import org.springframework.transaction.support.TransactionTemplate;

@Component
@Slf4j
public class NotificationEventListener {

    private final NotificationService notificationService;
    private final TransactionTemplate transactionTemplate;

    public NotificationEventListener(
            NotificationService notificationService,
            PlatformTransactionManager transactionManager) {
        this.notificationService = notificationService;
        this.transactionTemplate = new TransactionTemplate(transactionManager);
        this.transactionTemplate.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRES_NEW);
    }

    @EventListener
    public void handle(NotificationEvent event) {
        if (TransactionSynchronizationManager.isActualTransactionActive()
                && TransactionSynchronizationManager.isSynchronizationActive()) {
            TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
                @Override
                public void afterCommit() {
                    handleAfterCommit(event);
                }
            });
            return;
        }

        handleAfterCommit(event);
    }

    private void handleAfterCommit(NotificationEvent event) {
        try {
            transactionTemplate.executeWithoutResult(status -> createNotification(event));
        } catch (RuntimeException e) {
            log.warn("Falha ao criar notificacao {} apos commit: {}", event.type(), e.getMessage(), e);
        }
    }

    private void createNotification(NotificationEvent event) {
        if (event.type() == NotificationType.LIKE) {
            notificationService.createLikeNotification(event.senderEmail(), event.postId());
        } else if (event.type() == NotificationType.COMMENT) {
            notificationService.createCommentNotification(event.senderEmail(), event.postId(), event.commentId());
        } else if (event.type() == NotificationType.REPLY) {
            notificationService.createReplyNotification(
                    event.senderEmail(),
                    event.postId(),
                    event.commentId(),
                    event.parentCommentId());
        } else if (event.type() == NotificationType.MENTION) {
            notificationService.createMentionNotification(
                    event.senderEmail(),
                    event.recipientUsername(),
                    event.postId(),
                    event.commentId());
        } else if (event.type() == NotificationType.FOLLOW) {
            notificationService.createFollowNotification(event.senderEmail(), event.followedUserId());
        }
    }
}
