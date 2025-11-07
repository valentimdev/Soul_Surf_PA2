package com.soulsurf.backend.entities;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "posts")
@Getter
@Setter
@NoArgsConstructor
public class Post {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // @Column(nullable = false)
    // private String titulo;

    @Column(nullable = false)
    private String descricao;


    @Column(nullable = false, columnDefinition = "BOOLEAN DEFAULT TRUE")
    private boolean publico = true;

    @Column(name = "caminho_foto")
    private String caminhoFoto;

    @Column(nullable = false, updatable = false)
    private LocalDateTime data;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "usuario_id", nullable = false)
    private User usuario;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "beach_id")
    private Beach beach;

    @OneToMany(mappedBy = "post", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Comment> comments = new ArrayList<>();

    @OneToMany(mappedBy = "post", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<com.soulsurf.backend.entities.Like> likes = new ArrayList<>();

    @PrePersist
    protected void onCreate() {
        this.data = LocalDateTime.now();
    }
}