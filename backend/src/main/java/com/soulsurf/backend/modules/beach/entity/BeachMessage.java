package com.soulsurf.backend.modules.beach.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import java.time.LocalDateTime;

import com.soulsurf.backend.modules.user.entity.User;

@Entity
@Table(name = "mensagens")
@Data
@NoArgsConstructor
public class BeachMessage {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // O texto da BeachMessage. Usamos TEXT para suportar mensagens mais longas
    @Column(nullable = false, columnDefinition = "TEXT")
    private String texto;

    // Data de criação, preenchida automaticamente
    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private LocalDateTime data;

    // Relação ManyToOne: Um usuário (Autor) pode ter muitas mensagens
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "autor_id", nullable = false)
    private User autor; // Usando 'User' por consistência no Spring Security

    // Relação ManyToOne: Uma praia pode ter muitas mensagens (Mural)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "praia_id", nullable = false)
    private Beach beach;
}

