package com.soulsurf.backend.modules.poi.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "points_of_interest")
@Getter
@Setter
@NoArgsConstructor
public class PointOfInterest {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String nome;

    @Lob
    private String descricao;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private PoiCategory categoria;

    @Column(nullable = false)
    private Double latitude;

    @Column(nullable = false)
    private Double longitude;

    private String telefone;

    @Column(name = "caminho_foto")
    private String caminhoFoto;

}
