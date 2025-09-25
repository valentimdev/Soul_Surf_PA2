package com.soulsurf.backend.entities;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "beaches")
@Getter
@Setter
@NoArgsConstructor
public class Beach {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String nome;

    @Column(columnDefinition = "TEXT")
    private String descricao;

    @Column(nullable = false)
    private String localizacao;

    @Column(name = "caminho_foto")
    private String caminhoFoto;

    @Column(name = "nivel_experiencia")
    private String nivelExperiencia;  // Iniciante, Intermediário, Avançado
}