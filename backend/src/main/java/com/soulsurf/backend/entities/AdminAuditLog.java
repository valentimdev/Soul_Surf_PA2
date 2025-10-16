package com.soulsurf.backend.entities;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name = "admin_audit_logs")
@Getter
@Setter
@NoArgsConstructor
public class AdminAuditLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String actorEmail; // quem realizou a ação (admin)

    @Column(nullable = false)
    private String action; // ex: DELETE_USER, DELETE_POST, BAN_USER, PROMOTE_TO_ADMIN

    @Column(nullable = false)
    private String targetType; // USER, POST, COMMENT

    @Column(nullable = false)
    private Long targetId;

    @Column
    private String details; // informação adicional opcional

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
    }
}


