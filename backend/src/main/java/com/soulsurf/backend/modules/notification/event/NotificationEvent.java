package com.soulsurf.backend.modules.notification.event;

import com.soulsurf.backend.modules.notification.entity.NotificationType;

public record NotificationEvent(
        NotificationType type,
        String senderEmail,
        Long followedUserId,
        String recipientUsername,
        Long postId,
        Long commentId,
        Long parentCommentId) {

    public static NotificationEvent like(String senderEmail, Long postId) {
        return new NotificationEvent(NotificationType.LIKE, senderEmail, null, null, postId, null, null);
    }

    public static NotificationEvent comment(String senderEmail, Long postId, Long commentId) {
        return new NotificationEvent(NotificationType.COMMENT, senderEmail, null, null, postId, commentId, null);
    }

    public static NotificationEvent reply(String senderEmail, Long postId, Long commentId, Long parentCommentId) {
        return new NotificationEvent(NotificationType.REPLY, senderEmail, null, null, postId, commentId, parentCommentId);
    }

    public static NotificationEvent mention(String senderEmail, String recipientUsername, Long postId, Long commentId) {
        return new NotificationEvent(NotificationType.MENTION, senderEmail, null, recipientUsername, postId, commentId,
                null);
    }

    public static NotificationEvent follow(String senderEmail, Long followedUserId) {
        return new NotificationEvent(NotificationType.FOLLOW, senderEmail, followedUserId, null, null, null, null);
    }
}
