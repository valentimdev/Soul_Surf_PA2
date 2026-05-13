package com.soulsurf.backend.modules.notification.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

import com.soulsurf.backend.modules.user.entity.User;
import com.soulsurf.backend.modules.post.entity.Post;
import com.soulsurf.backend.modules.comment.entity.Comment;

@Entity
@Table(name = "notifications")
@Getter
@Setter
@NoArgsConstructor
public class Notification {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "recipient_id", nullable = false)
    private User recipient;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "sender_id", nullable = false)
    private User sender;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private NotificationType type;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "post_id")
    private Post post;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "comment_id")
    private Comment comment;

    @Column(name = "is_read", nullable = false)
    private boolean read;

    // Legacy Oracle column still exists in production and is NOT NULL.
    @Column(name = "read", nullable = false)
    private boolean legacyRead;

    @Column(nullable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
        setRead(false);
    }

    @PreUpdate
    protected void onUpdate() {
        this.legacyRead = this.read;
    }

    public void setRead(boolean read) {
        this.read = read;
        this.legacyRead = read;
    }
}
