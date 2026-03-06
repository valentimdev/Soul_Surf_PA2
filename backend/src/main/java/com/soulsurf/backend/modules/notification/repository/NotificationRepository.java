package com.soulsurf.backend.modules.notification.repository;

import com.soulsurf.backend.modules.comment.entity.Comment;
import com.soulsurf.backend.modules.notification.entity.Notification;
import com.soulsurf.backend.modules.post.entity.Post;
import com.soulsurf.backend.modules.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface NotificationRepository extends JpaRepository<Notification, Long> {

    @Query("""
        SELECT n FROM Notification n
        JOIN FETCH n.sender
        WHERE n.recipient = :recipient
        ORDER BY n.createdAt DESC
        """)
    List<Notification> findByRecipientOrderByCreatedAtDesc(@Param("recipient") User recipient);

    int countByRecipientAndReadFalse(User recipient);

    List<Notification> findByRecipientAndReadFalseOrderByCreatedAtDesc(User recipient);

    List<Notification> findByRecipientAndReadTrueOrderByCreatedAtDesc(User recipient);

    void deleteByPost(Post post);
    void deleteByComment(Comment comment);

    void deleteAllByPost(Post post);
    void deleteAllByCommentIn(List<Comment> comments);
}

