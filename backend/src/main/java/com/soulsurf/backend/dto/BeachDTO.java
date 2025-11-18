package com.soulsurf.backend.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class BeachDTO {
    private Long id;
    private String nome;
    private String descricao;
    private String localizacao;
    private String caminhoFoto;
    private String nivelExperiencia;
}