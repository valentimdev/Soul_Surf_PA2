package com.soulsurf.backend.modules.notification.event;

import com.soulsurf.backend.modules.notification.entity.NotificationType;
import com.soulsurf.backend.modules.notification.service.NotificationService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class NotificationEventListener {

    private final NotificationService notificationService;

    public NotificationEventListener(NotificationService notificationService) {
        this.notificationService = notificationService;
    }

    @EventListener
    public void handle(NotificationEvent event) {
        try {
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
        } catch (RuntimeException e) {
            log.warn("Falha ao criar notificacao {} apos commit: {}", event.type(), e.getMessage(), e);
        }
    }
}
